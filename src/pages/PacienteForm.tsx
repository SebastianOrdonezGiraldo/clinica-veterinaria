import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';
import { ArrowLeft, Save, Upload, X } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Textarea } from '@/components/ui/textarea';
import { useToast } from '@/hooks/use-toast';
import { mockPropietarios, getPacienteById } from '@/lib/mockData';

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

  const { register, handleSubmit, setValue, formState: { errors }, reset } = useForm<PacienteFormData>({
    resolver: zodResolver(pacienteSchema),
  });

  const [imagePreview, setImagePreview] = useState<string | null>(null);

  // Pre-llenar el formulario en modo edición
  useEffect(() => {
    if (isEdit && id) {
      const paciente = getPacienteById(id);
      if (paciente) {
        reset({
          nombre: paciente.nombre,
          especie: paciente.especie as any,
          raza: paciente.raza || '',
          sexo: paciente.sexo || undefined,
          edadMeses: paciente.edadMeses || undefined,
          pesoKg: paciente.pesoKg || undefined,
          propietarioId: paciente.propietarioId,
          microchip: paciente.microchip || '',
          notas: '',
        });
      }
    }
  }, [isEdit, id, reset]);

  const handleImageChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      if (file.size > 5 * 1024 * 1024) {
        toast({
          title: 'Error',
          description: 'La imagen no debe superar los 5MB',
          variant: 'destructive',
        });
        return;
      }

      const reader = new FileReader();
      reader.onloadend = () => {
        setImagePreview(reader.result as string);
      };
      reader.readAsDataURL(file);
    }
  };

  const removeImage = () => {
    setImagePreview(null);
  };

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
            {/* Upload de imagen */}
            <div className="space-y-2">
              <Label>Foto del Paciente</Label>
              <div className="flex items-start gap-4">
                {imagePreview ? (
                  <div className="relative">
                    <img
                      src={imagePreview}
                      alt="Preview"
                      className="h-32 w-32 rounded-lg object-cover border-2 border-border"
                    />
                    <Button
                      type="button"
                      variant="destructive"
                      size="icon"
                      className="absolute -top-2 -right-2 h-6 w-6 rounded-full"
                      onClick={removeImage}
                    >
                      <X className="h-3 w-3" />
                    </Button>
                  </div>
                ) : (
                  <div className="h-32 w-32 rounded-lg border-2 border-dashed border-border flex items-center justify-center bg-accent/50">
                    <Upload className="h-8 w-8 text-muted-foreground" />
                  </div>
                )}
                <div className="flex-1 space-y-2">
                  <Input
                    type="file"
                    accept="image/*"
                    onChange={handleImageChange}
                    className="cursor-pointer"
                  />
                  <p className="text-xs text-muted-foreground">
                    Formatos aceptados: JPG, PNG, GIF. Tamaño máximo: 5MB
                  </p>
                </div>
              </div>
            </div>

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
