import { useNavigate, useParams } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';
import { ArrowLeft, Save } from 'lucide-react';
import { Button } from '@shared/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@shared/components/ui/card';
import { Input } from '@shared/components/ui/input';
import { Label } from '@shared/components/ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@shared/components/ui/select';
import { Textarea } from '@shared/components/ui/textarea';
import { useToast } from '@shared/hooks/use-toast';
import { mockPacientes, mockPropietarios, mockUsuarios } from '@shared/utils/mockData';

const citaSchema = z.object({
  pacienteId: z.string().min(1, 'Paciente es requerido'),
  propietarioId: z.string().min(1, 'Propietario es requerido'),
  profesionalId: z.string().min(1, 'Profesional es requerido'),
  fecha: z.string().min(1, 'Fecha es requerida'),
  hora: z.string().min(1, 'Hora es requerida'),
  estado: z.enum(['Pendiente', 'Confirmada', 'Cancelada', 'Atendida']),
  motivo: z.string().max(500).optional(),
});

type CitaFormData = z.infer<typeof citaSchema>;

export default function CitaForm() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { toast } = useToast();
  const isEdit = !!id;

  const veterinarios = mockUsuarios.filter(u => u.rol === 'VET' || u.rol === 'ADMIN');

  const { register, handleSubmit, setValue, watch, formState: { errors } } = useForm<CitaFormData>({
    resolver: zodResolver(citaSchema),
    defaultValues: {
      estado: 'Pendiente',
    },
  });

  const pacienteSeleccionado = watch('pacienteId');

  const handlePacienteChange = (pacienteId: string) => {
    setValue('pacienteId', pacienteId);
    const paciente = mockPacientes.find(p => p.id === pacienteId);
    if (paciente) {
      setValue('propietarioId', paciente.propietarioId);
    }
  };

  const onSubmit = (data: CitaFormData) => {
    const fechaHora = `${data.fecha}T${data.hora}:00`;
    console.log('Guardar cita:', { ...data, fecha: fechaHora });
    toast({
      title: isEdit ? 'Cita actualizada' : 'Cita agendada',
      description: `La cita ha sido ${isEdit ? 'actualizada' : 'agendada'} exitosamente.`,
    });
    navigate('/agenda');
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center gap-4">
        <Button variant="ghost" size="icon" onClick={() => navigate('/agenda')}>
          <ArrowLeft className="h-4 w-4" />
        </Button>
        <div>
          <h1 className="text-3xl font-bold text-foreground">
            {isEdit ? 'Editar Cita' : 'Nueva Cita'}
          </h1>
          <p className="text-muted-foreground mt-1">Complete la información de la cita</p>
        </div>
      </div>

      <form onSubmit={handleSubmit(onSubmit)}>
        <Card>
          <CardHeader>
            <CardTitle>Información de la Cita</CardTitle>
          </CardHeader>
          <CardContent className="space-y-6">
            <div className="grid gap-4 md:grid-cols-2">
              <div className="space-y-2">
                <Label htmlFor="pacienteId">Paciente *</Label>
                <Select onValueChange={handlePacienteChange}>
                  <SelectTrigger>
                    <SelectValue placeholder="Seleccione paciente" />
                  </SelectTrigger>
                  <SelectContent>
                    {mockPacientes.map((paciente) => (
                      <SelectItem key={paciente.id} value={paciente.id}>
                        {paciente.nombre} ({paciente.especie})
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
                {errors.pacienteId && (
                  <p className="text-sm text-destructive">{errors.pacienteId.message}</p>
                )}
              </div>

              <div className="space-y-2">
                <Label htmlFor="propietarioId">Propietario *</Label>
                <Select 
                  value={watch('propietarioId')} 
                  onValueChange={(value) => setValue('propietarioId', value)}
                  disabled={!!pacienteSeleccionado}
                >
                  <SelectTrigger>
                    <SelectValue placeholder="Seleccione propietario" />
                  </SelectTrigger>
                  <SelectContent>
                    {mockPropietarios.map((prop) => (
                      <SelectItem key={prop.id} value={prop.id}>
                        {prop.nombre}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
                {errors.propietarioId && (
                  <p className="text-sm text-destructive">{errors.propietarioId.message}</p>
                )}
              </div>

              <div className="space-y-2">
                <Label htmlFor="profesionalId">Veterinario *</Label>
                <Select onValueChange={(value) => setValue('profesionalId', value)}>
                  <SelectTrigger>
                    <SelectValue placeholder="Seleccione veterinario" />
                  </SelectTrigger>
                  <SelectContent>
                    {veterinarios.map((vet) => (
                      <SelectItem key={vet.id} value={vet.id}>
                        {vet.nombre}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
                {errors.profesionalId && (
                  <p className="text-sm text-destructive">{errors.profesionalId.message}</p>
                )}
              </div>

              <div className="space-y-2">
                <Label htmlFor="estado">Estado</Label>
                <Select onValueChange={(value) => setValue('estado', value as any)} defaultValue="Pendiente">
                  <SelectTrigger>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="Pendiente">Pendiente</SelectItem>
                    <SelectItem value="Confirmada">Confirmada</SelectItem>
                    <SelectItem value="Cancelada">Cancelada</SelectItem>
                    <SelectItem value="Atendida">Atendida</SelectItem>
                  </SelectContent>
                </Select>
              </div>

              <div className="space-y-2">
                <Label htmlFor="fecha">Fecha *</Label>
                <Input
                  id="fecha"
                  type="date"
                  {...register('fecha')}
                />
                {errors.fecha && (
                  <p className="text-sm text-destructive">{errors.fecha.message}</p>
                )}
              </div>

              <div className="space-y-2">
                <Label htmlFor="hora">Hora *</Label>
                <Input
                  id="hora"
                  type="time"
                  {...register('hora')}
                />
                {errors.hora && (
                  <p className="text-sm text-destructive">{errors.hora.message}</p>
                )}
              </div>
            </div>

            <div className="space-y-2">
              <Label htmlFor="motivo">Motivo de la Consulta</Label>
              <Textarea
                id="motivo"
                {...register('motivo')}
                placeholder="Describa el motivo de la consulta"
                rows={4}
              />
            </div>

            <div className="flex justify-end gap-3 pt-4">
              <Button type="button" variant="outline" onClick={() => navigate('/agenda')}>
                Cancelar
              </Button>
              <Button type="submit" className="gap-2">
                <Save className="h-4 w-4" />
                {isEdit ? 'Actualizar' : 'Agendar'}
              </Button>
            </div>
          </CardContent>
        </Card>
      </form>
    </div>
  );
}
