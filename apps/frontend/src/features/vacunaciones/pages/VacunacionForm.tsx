import { useState, useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { ArrowLeft, Save, Calendar } from 'lucide-react';
import { Button } from '@shared/components/ui/button';
import { Input } from '@shared/components/ui/input';
import { Textarea } from '@shared/components/ui/textarea';
import { Card, CardContent, CardHeader, CardTitle } from '@shared/components/ui/card';
import { Form, FormControl, FormDescription, FormField, FormItem, FormLabel, FormMessage } from '@shared/components/ui/form';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@shared/components/ui/select';
import { useCreateVacunacion, useUpdateVacunacion } from '../hooks/useVacunaciones';
import { useVacunasActivas } from '../hooks/useVacunas';
import { useAllPacientes } from '@features/pacientes/hooks/usePacientes';
import { useQuery } from '@tanstack/react-query';
import { usuarioService } from '@features/usuarios/services/usuarioService';
import { useAuth } from '@core/auth/AuthContext';
import { LoadingCards } from '@shared/components/common/LoadingCards';
import { Vacuna } from '@core/types';

const vacunacionSchema = z.object({
  pacienteId: z.string().min(1, 'El paciente es requerido'),
  vacunaId: z.string().min(1, 'La vacuna es requerida'),
  profesionalId: z.string().min(1, 'El profesional es requerido'),
  fechaAplicacion: z.string().min(1, 'La fecha de aplicación es requerida'),
  numeroDosis: z.number().int().positive('El número de dosis debe ser mayor a 0'),
  lote: z.string().max(50, 'El lote no puede exceder 50 caracteres').optional().or(z.literal('')),
  observaciones: z.string().max(500, 'Las observaciones no pueden exceder 500 caracteres').optional().or(z.literal('')),
});

type VacunacionFormData = z.infer<typeof vacunacionSchema>;

export default function VacunacionForm() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const pacienteIdFromUrl = searchParams.get('pacienteId') || '';
  const { user } = useAuth();

  const { data: vacunas = [], isLoading: isLoadingVacunas, error: errorVacunas } = useVacunasActivas();
  const { pacientes = [] } = useAllPacientes();
  const { data: profesionales = [] } = useQuery({
    queryKey: ['usuarios', 'veterinarios'],
    queryFn: () => usuarioService.getVeterinarios(),
  });

  const { mutate: createVacunacion, isPending: isCreating } = useCreateVacunacion();

  const form = useForm<VacunacionFormData>({
    resolver: zodResolver(vacunacionSchema),
    defaultValues: {
      pacienteId: pacienteIdFromUrl,
      vacunaId: '',
      profesionalId: user?.id || '',
      fechaAplicacion: new Date().toISOString().split('T')[0],
      numeroDosis: 1,
      lote: '',
      observaciones: '',
    },
  });

  const vacunaSeleccionada = form.watch('vacunaId');
  const pacienteSeleccionado = form.watch('pacienteId');
  const numeroDosis = form.watch('numeroDosis');

  // Cargar vacuna seleccionada para validar número de dosis
  const vacuna = vacunas.find(v => v.id === vacunaSeleccionada);

  // Ajustar número de dosis si excede el máximo
  useEffect(() => {
    if (vacuna && numeroDosis > vacuna.numeroDosis) {
      form.setValue('numeroDosis', vacuna.numeroDosis);
    }
  }, [vacuna, numeroDosis, form]);

  // Filtrar vacunas por especie del paciente
  // Si no hay paciente seleccionado, mostrar todas las vacunas activas
  // Si hay paciente, mostrar vacunas sin especie específica o que coincidan con la especie del paciente
  const vacunasDisponibles = vacunas.filter(v => {
    if (!v.activo) return false; // Solo mostrar vacunas activas
    if (!pacienteSeleccionado) return true; // Sin paciente, mostrar todas
    const paciente = pacientes.find(p => p.id === pacienteSeleccionado);
    if (!paciente) return true; // Paciente no encontrado, mostrar todas
    // Si la vacuna no tiene especie específica (null o vacío) o coincide con la del paciente
    return !v.especie || v.especie.trim() === '' || v.especie === paciente.especie;
  });

  const onSubmit = (data: VacunacionFormData) => {
    if (vacuna && data.numeroDosis > vacuna.numeroDosis) {
      form.setError('numeroDosis', {
        type: 'manual',
        message: `El número de dosis no puede exceder ${vacuna.numeroDosis}`,
      });
      return;
    }

    const dto = {
      ...data,
      lote: data.lote || undefined,
      observaciones: data.observaciones || undefined,
    };

    createVacunacion(dto, {
      onSuccess: () => navigate('/vacunaciones'),
    });
  };

  if (isLoadingVacunas) {
    return <LoadingCards count={1} />;
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center gap-4">
        <Button variant="ghost" size="icon" onClick={() => navigate('/vacunaciones')}>
          <ArrowLeft className="h-4 w-4" />
        </Button>
        <div>
          <h1 className="text-3xl font-bold text-foreground">Nueva Vacunación</h1>
          <p className="text-muted-foreground mt-1">Registra una nueva vacunación para un paciente</p>
        </div>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Información de la Vacunación</CardTitle>
        </CardHeader>
        <CardContent>
          <Form {...form}>
            <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-6">
              <FormField
                control={form.control}
                name="pacienteId"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Paciente *</FormLabel>
                    <Select onValueChange={field.onChange} value={field.value}>
                      <FormControl>
                        <SelectTrigger>
                          <SelectValue placeholder="Selecciona un paciente" />
                        </SelectTrigger>
                      </FormControl>
                      <SelectContent>
                        {pacientes.map((paciente) => (
                          <SelectItem key={paciente.id} value={paciente.id}>
                            {paciente.nombre} - {paciente.especie}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                    <FormMessage />
                  </FormItem>
                )}
              />

              <FormField
                control={form.control}
                name="vacunaId"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Vacuna *</FormLabel>
                    <Select onValueChange={field.onChange} value={field.value}>
                      <FormControl>
                        <SelectTrigger>
                          <SelectValue placeholder="Selecciona una vacuna" />
                        </SelectTrigger>
                      </FormControl>
                      <SelectContent>
                        {isLoadingVacunas ? (
                          <div className="px-2 py-1.5 text-sm text-muted-foreground">
                            Cargando vacunas...
                          </div>
                        ) : errorVacunas ? (
                          <div className="px-2 py-1.5 text-sm text-destructive">
                            Error al cargar vacunas
                          </div>
                        ) : vacunasDisponibles.length === 0 ? (
                          <div className="px-2 py-1.5">
                            {vacunas.length === 0 ? (
                              <div className="space-y-2">
                                <p className="text-sm text-muted-foreground">
                                  No hay vacunas registradas.
                                </p>
                                <Button
                                  type="button"
                                  variant="outline"
                                  size="sm"
                                  onClick={() => navigate('/vacunaciones/vacunas/nueva')}
                                  className="w-full"
                                >
                                  Crear Primera Vacuna
                                </Button>
                              </div>
                            ) : pacienteSeleccionado ? (
                              <p className="text-sm text-muted-foreground">
                                No hay vacunas disponibles para esta especie
                              </p>
                            ) : (
                              <p className="text-sm text-muted-foreground">
                                No hay vacunas activas disponibles
                              </p>
                            )}
                          </div>
                        ) : (
                          vacunasDisponibles.map((vac) => (
                            <SelectItem key={vac.id} value={vac.id}>
                              {vac.nombre} {vac.especie ? `(${vac.especie})` : '(Todas las especies)'} - {vac.numeroDosis} dosis
                            </SelectItem>
                          ))
                        )}
                      </SelectContent>
                    </Select>
                    {vacuna && (
                      <FormDescription>
                        Esta vacuna requiere {vacuna.numeroDosis} dosis
                        {vacuna.intervaloDias && ` con intervalo de ${vacuna.intervaloDias} días entre dosis`}
                      </FormDescription>
                    )}
                    <FormMessage />
                  </FormItem>
                )}
              />

              <div className="grid gap-4 md:grid-cols-2">
                <FormField
                  control={form.control}
                  name="fechaAplicacion"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>Fecha de Aplicación *</FormLabel>
                      <FormControl>
                        <Input type="date" {...field} max={new Date().toISOString().split('T')[0]} />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />

                <FormField
                  control={form.control}
                  name="numeroDosis"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>Número de Dosis *</FormLabel>
                      <FormControl>
                        <Input
                          type="number"
                          min="1"
                          max={vacuna?.numeroDosis || 10}
                          step="1"
                          {...field}
                          onChange={(e) => field.onChange(parseInt(e.target.value) || 1)}
                        />
                      </FormControl>
                      {vacuna && (
                        <FormDescription>
                          Dosis {field.value} de {vacuna.numeroDosis}
                        </FormDescription>
                      )}
                      <FormMessage />
                    </FormItem>
                  )}
                />
              </div>

              <FormField
                control={form.control}
                name="profesionalId"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Profesional *</FormLabel>
                    <Select onValueChange={field.onChange} value={field.value}>
                      <FormControl>
                        <SelectTrigger>
                          <SelectValue placeholder="Selecciona un profesional" />
                        </SelectTrigger>
                      </FormControl>
                      <SelectContent>
                        {profesionales.map((prof) => (
                          <SelectItem key={prof.id} value={prof.id}>
                            {prof.nombre}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                    <FormMessage />
                  </FormItem>
                )}
              />

              <FormField
                control={form.control}
                name="lote"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Lote</FormLabel>
                    <FormControl>
                      <Input placeholder="Número de lote (opcional)" {...field} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />

              <FormField
                control={form.control}
                name="observaciones"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Observaciones</FormLabel>
                    <FormControl>
                      <Textarea
                        placeholder="Observaciones adicionales (opcional)..."
                        className="resize-none"
                        rows={4}
                        {...field}
                      />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />

              <div className="flex justify-end gap-4">
                <Button
                  type="button"
                  variant="outline"
                  onClick={() => navigate('/vacunaciones')}
                >
                  Cancelar
                </Button>
                <Button type="submit" disabled={isCreating} className="gap-2">
                  <Save className="h-4 w-4" />
                  {isCreating ? 'Registrando...' : 'Registrar Vacunación'}
                </Button>
              </div>
            </form>
          </Form>
        </CardContent>
      </Card>
    </div>
  );
}

