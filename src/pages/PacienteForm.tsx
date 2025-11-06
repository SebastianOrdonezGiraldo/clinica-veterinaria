import { useNavigate, useParams } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';
import { ArrowLeft, Save } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Textarea } from '@/components/ui/textarea';
import { useToast } from '@/hooks/use-toast';
import { mockPropietarios } from '@/lib/mockData';

const pacienteSchema = z.object({
  nombre: z.string().min(1, 'Nombre es requerido').max(100),
  especie: z.enum(['Canino', 'Felino', 'Otro'], { required_error: 'Especie es requerida' }),
  raza: z.string().max(100).optional(),
  sexo: z.enum(['M', 'F']).optional(),
  edadMeses: z.number().int().positive().optional(),
  pesoKg: z.number().positive().optional(),
  propietarioId: z.string().min(1, 'Propietario es requerido'),
  microchip: z.string().max(50).optional(),
  notas: z.string().max(500).optional(),
});

type PacienteFormData = z.infer<typeof pacienteSchema>;

export default function PacienteForm() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { toast } = useToast();
  const isEdit = !!id;

  const { register, handleSubmit, setValue, formState: { errors } } = useForm<PacienteFormData>({
    resolver: zodResolver(pacienteSchema),
  });

  const onSubmit = (data: PacienteFormData) => {
    console.log('Guardar paciente:', data);
    toast({
      title: isEdit ? 'Paciente actualizado' : 'Paciente registrado',
      description: `${data.nombre} ha sido ${isEdit ? 'actualizado' : 'registrado'} exitosamente.`,
    });
    navigate('/pacientes');
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center gap-4">
        <Button variant="ghost" size="icon" onClick={() => navigate('/pacientes')}>
          <ArrowLeft className="h-4 w-4" />
        </Button>
        <div>
          <h1 className="text-3xl font-bold text-foreground">
            {isEdit ? 'Editar Paciente' : 'Nuevo Paciente'}
          </h1>
          <p className="text-muted-foreground mt-1">Complete la información del paciente</p>
        </div>
      </div>

      <form onSubmit={handleSubmit(onSubmit)}>
        <Card>
          <CardHeader>
            <CardTitle>Información del Paciente</CardTitle>
          </CardHeader>
          <CardContent className="space-y-6">
            <div className="grid gap-4 md:grid-cols-2">
              <div className="space-y-2">
                <Label htmlFor="nombre">Nombre *</Label>
                <Input
                  id="nombre"
                  {...register('nombre')}
                  placeholder="Nombre del paciente"
                />
                {errors.nombre && (
                  <p className="text-sm text-destructive">{errors.nombre.message}</p>
                )}
              </div>

              <div className="space-y-2">
                <Label htmlFor="propietarioId">Propietario *</Label>
                <Select onValueChange={(value) => setValue('propietarioId', value)}>
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
                <Label htmlFor="especie">Especie *</Label>
                <Select onValueChange={(value) => setValue('especie', value as any)}>
                  <SelectTrigger>
                    <SelectValue placeholder="Seleccione especie" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="Canino">Canino</SelectItem>
                    <SelectItem value="Felino">Felino</SelectItem>
                    <SelectItem value="Otro">Otro</SelectItem>
                  </SelectContent>
                </Select>
                {errors.especie && (
                  <p className="text-sm text-destructive">{errors.especie.message}</p>
                )}
              </div>

              <div className="space-y-2">
                <Label htmlFor="raza">Raza</Label>
                <Input
                  id="raza"
                  {...register('raza')}
                  placeholder="Raza del paciente"
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="sexo">Sexo</Label>
                <Select onValueChange={(value) => setValue('sexo', value as any)}>
                  <SelectTrigger>
                    <SelectValue placeholder="Seleccione sexo" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="M">Macho</SelectItem>
                    <SelectItem value="F">Hembra</SelectItem>
                  </SelectContent>
                </Select>
              </div>

              <div className="space-y-2">
                <Label htmlFor="edadMeses">Edad (meses)</Label>
                <Input
                  id="edadMeses"
                  type="number"
                  {...register('edadMeses', { valueAsNumber: true })}
                  placeholder="Edad en meses"
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="pesoKg">Peso (kg)</Label>
                <Input
                  id="pesoKg"
                  type="number"
                  step="0.1"
                  {...register('pesoKg', { valueAsNumber: true })}
                  placeholder="Peso en kilogramos"
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="microchip">Microchip</Label>
                <Input
                  id="microchip"
                  {...register('microchip')}
                  placeholder="Número de microchip"
                />
              </div>
            </div>

            <div className="space-y-2">
              <Label htmlFor="notas">Notas</Label>
              <Textarea
                id="notas"
                {...register('notas')}
                placeholder="Notas adicionales sobre el paciente"
                rows={4}
              />
            </div>

            <div className="flex justify-end gap-3 pt-4">
              <Button type="button" variant="outline" onClick={() => navigate('/pacientes')}>
                Cancelar
              </Button>
              <Button type="submit" className="gap-2">
                <Save className="h-4 w-4" />
                {isEdit ? 'Actualizar' : 'Guardar'}
              </Button>
            </div>
          </CardContent>
        </Card>
      </form>
    </div>
  );
}
