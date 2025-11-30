import { useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { ArrowLeft, Save } from 'lucide-react';
import { Button } from '@shared/components/ui/button';
import { Input } from '@shared/components/ui/input';
import { Textarea } from '@shared/components/ui/textarea';
import { Card, CardContent, CardHeader, CardTitle } from '@shared/components/ui/card';
import { Form, FormControl, FormDescription, FormField, FormItem, FormLabel, FormMessage } from '@shared/components/ui/form';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@shared/components/ui/select';
import { Switch } from '@shared/components/ui/switch';
import { useVacuna, useCreateVacuna, useUpdateVacuna } from '../hooks/useVacunas';
import { LoadingCards } from '@shared/components/common/LoadingCards';

const vacunaSchema = z.object({
  nombre: z.string().min(1, 'El nombre es requerido').max(100, 'El nombre no puede exceder 100 caracteres'),
  especie: z.string().max(50, 'La especie no puede exceder 50 caracteres').optional().or(z.literal('')),
  numeroDosis: z.number().int().positive('El número de dosis debe ser mayor a 0'),
  intervaloDias: z.number().int().positive('El intervalo debe ser mayor a 0').optional().or(z.literal('')),
  descripcion: z.string().max(500, 'La descripción no puede exceder 500 caracteres').optional().or(z.literal('')),
  fabricante: z.string().max(100, 'El fabricante no puede exceder 100 caracteres').optional().or(z.literal('')),
  activo: z.boolean().default(true),
});

type VacunaFormData = z.infer<typeof vacunaSchema>;

export default function VacunaForm() {
  const navigate = useNavigate();
  const { id } = useParams<{ id: string }>();
  const isEditing = !!id;

  const { data: vacuna, isLoading: isLoadingVacuna } = useVacuna(id);
  const { mutate: createVacuna, isPending: isCreating } = useCreateVacuna();
  const { mutate: updateVacuna, isPending: isUpdating } = useUpdateVacuna();

  const form = useForm<VacunaFormData>({
    resolver: zodResolver(vacunaSchema),
    defaultValues: {
      nombre: '',
      especie: '',
      numeroDosis: 1,
      intervaloDias: undefined,
      descripcion: '',
      fabricante: '',
      activo: true,
    },
  });

  useEffect(() => {
    if (vacuna) {
      form.reset({
        nombre: vacuna.nombre,
        especie: vacuna.especie || '',
        numeroDosis: vacuna.numeroDosis,
        intervaloDias: vacuna.intervaloDias || undefined,
        descripcion: vacuna.descripcion || '',
        fabricante: vacuna.fabricante || '',
        activo: vacuna.activo ?? true,
      });
    }
  }, [vacuna, form]);

  const onSubmit = (data: VacunaFormData) => {
    const dto = {
      ...data,
      especie: data.especie || undefined,
      intervaloDias: data.intervaloDias || undefined,
      descripcion: data.descripcion || undefined,
      fabricante: data.fabricante || undefined,
    };

    if (isEditing && id) {
      updateVacuna(
        { id, dto },
        {
          onSuccess: () => navigate('/vacunaciones/vacunas'),
        }
      );
    } else {
      createVacuna(dto, {
        onSuccess: () => navigate('/vacunaciones/vacunas'),
      });
    }
  };

  const especies = ['Canino', 'Felino', 'Ave', 'Roedor', 'Reptil', 'Otro'];

  if (isEditing && isLoadingVacuna) {
    return <LoadingCards count={1} />;
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center gap-4">
        <Button variant="ghost" size="icon" onClick={() => navigate('/vacunaciones/vacunas')}>
          <ArrowLeft className="h-4 w-4" />
        </Button>
        <div>
          <h1 className="text-3xl font-bold text-foreground">
            {isEditing ? 'Editar Vacuna' : 'Nueva Vacuna'}
          </h1>
          <p className="text-muted-foreground mt-1">
            {isEditing ? 'Modifica los datos de la vacuna' : 'Registra un nuevo tipo de vacuna'}
          </p>
        </div>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Información de la Vacuna</CardTitle>
        </CardHeader>
        <CardContent>
          <Form {...form}>
            <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-6">
              <FormField
                control={form.control}
                name="nombre"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Nombre *</FormLabel>
                    <FormControl>
                      <Input placeholder="Ej: Antirrábica" {...field} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />

              <FormField
                control={form.control}
                name="especie"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Especie</FormLabel>
                    <Select onValueChange={field.onChange} value={field.value || ''}>
                      <FormControl>
                        <SelectTrigger>
                          <SelectValue placeholder="Selecciona una especie (opcional)" />
                        </SelectTrigger>
                      </FormControl>
                      <SelectContent>
                        <SelectItem value="">Todas las especies</SelectItem>
                        {especies.map((esp) => (
                          <SelectItem key={esp} value={esp}>
                            {esp}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                    <FormDescription>
                      Deja vacío si la vacuna aplica para todas las especies
                    </FormDescription>
                    <FormMessage />
                  </FormItem>
                )}
              />

              <div className="grid gap-4 md:grid-cols-2">
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
                          step="1"
                          {...field}
                          onChange={(e) => field.onChange(parseInt(e.target.value) || 1)}
                        />
                      </FormControl>
                      <FormDescription>Total de dosis requeridas para esta vacuna</FormDescription>
                      <FormMessage />
                    </FormItem>
                  )}
                />

                <FormField
                  control={form.control}
                  name="intervaloDias"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>Intervalo entre Dosis (días)</FormLabel>
                      <FormControl>
                        <Input
                          type="number"
                          min="1"
                          step="1"
                          {...field}
                          value={field.value || ''}
                          onChange={(e) => field.onChange(e.target.value ? parseInt(e.target.value) : undefined)}
                          onBlur={() => {
                            if (!field.value) field.onChange(undefined);
                          }}
                        />
                      </FormControl>
                      <FormDescription>Días entre cada dosis (opcional)</FormDescription>
                      <FormMessage />
                    </FormItem>
                  )}
                />
              </div>

              <FormField
                control={form.control}
                name="fabricante"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Fabricante</FormLabel>
                    <FormControl>
                      <Input placeholder="Ej: Laboratorio XYZ" {...field} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />

              <FormField
                control={form.control}
                name="descripcion"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Descripción</FormLabel>
                    <FormControl>
                      <Textarea
                        placeholder="Información adicional sobre la vacuna..."
                        className="resize-none"
                        rows={4}
                        {...field}
                      />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />

              <FormField
                control={form.control}
                name="activo"
                render={({ field }) => (
                  <FormItem className="flex flex-row items-center justify-between rounded-lg border p-4">
                    <div className="space-y-0.5">
                      <FormLabel className="text-base">Activa</FormLabel>
                      <FormDescription>
                        Las vacunas inactivas no aparecerán al registrar nuevas vacunaciones
                      </FormDescription>
                    </div>
                    <FormControl>
                      <Switch
                        checked={field.value}
                        onCheckedChange={field.onChange}
                      />
                    </FormControl>
                  </FormItem>
                )}
              />

              <div className="flex justify-end gap-4">
                <Button
                  type="button"
                  variant="outline"
                  onClick={() => navigate('/vacunaciones/vacunas')}
                >
                  Cancelar
                </Button>
                <Button type="submit" disabled={isCreating || isUpdating} className="gap-2">
                  <Save className="h-4 w-4" />
                  {isCreating || isUpdating ? 'Guardando...' : 'Guardar'}
                </Button>
              </div>
            </form>
          </Form>
        </CardContent>
      </Card>
    </div>
  );
}

