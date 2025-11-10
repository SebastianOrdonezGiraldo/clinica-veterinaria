import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';
import { ArrowLeft, Save, Upload, X } from 'lucide-react';
import { Button } from '@shared/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@shared/components/ui/card';
import { Input } from '@shared/components/ui/input';
import { Label } from '@shared/components/ui/label';
import { Textarea } from '@shared/components/ui/textarea';
import { toast } from 'sonner';
import { propietarioService } from '@features/propietarios/services/propietarioService';

const propietarioSchema = z.object({
  nombre: z.string().min(1, 'Nombre es requerido').max(100),
  documento: z.string().max(20).optional(),
  email: z.string().email('Email inválido').max(100).optional().or(z.literal('')),
  telefono: z.string().max(20).optional(),
  direccion: z.string().max(200).optional(),
});

type PropietarioFormData = z.infer<typeof propietarioSchema>;

export default function PropietarioForm() {
  const { id } = useParams();
  const navigate = useNavigate();
  const isEdit = !!id;

  const { register, handleSubmit, formState: { errors }, reset } = useForm<PropietarioFormData>({
    resolver: zodResolver(propietarioSchema),
  });

  const [imagePreview, setImagePreview] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [isLoadingData, setIsLoadingData] = useState(isEdit);

  // Pre-llenar el formulario en modo edición
  useEffect(() => {
    if (isEdit && id) {
      loadPropietario();
    }
  }, [isEdit, id]);

  const loadPropietario = async () => {
    if (!id) return;
    
    try {
      setIsLoadingData(true);
      const propietario = await propietarioService.getById(id);
      reset({
        nombre: propietario.nombre,
        documento: propietario.documento || '',
        email: propietario.email || '',
        telefono: propietario.telefono || '',
        direccion: propietario.direccion || '',
      });
    } catch (error) {
      console.error('Error al cargar propietario:', error);
      toast.error('Error al cargar los datos del propietario');
    } finally {
      setIsLoadingData(false);
    }
  };

  const handleImageChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      if (file.size > 5 * 1024 * 1024) {
        toast.error('La imagen no debe superar los 5MB');
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

  const onSubmit = async (data: PropietarioFormData) => {
    try {
      setIsLoading(true);
      
      // Preparar datos para enviar (convertir strings vacíos a undefined para campos opcionales)
      const propietarioData = {
        nombre: data.nombre,
        documento: data.documento || undefined,
        email: data.email || undefined,
        telefono: data.telefono || undefined,
        direccion: data.direccion || undefined,
      };

      if (isEdit && id) {
        await propietarioService.update(id, propietarioData);
        toast.success('Propietario actualizado exitosamente');
      } else {
        await propietarioService.create(propietarioData);
        toast.success('Propietario registrado exitosamente');
      }
      
      navigate('/propietarios');
    } catch (error: any) {
      console.error('Error al guardar propietario:', error);
      toast.error(error.response?.data?.message || `Error al ${isEdit ? 'actualizar' : 'registrar'} el propietario`);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center gap-4">
        <Button variant="ghost" size="icon" onClick={() => navigate('/propietarios')}>
          <ArrowLeft className="h-4 w-4" />
        </Button>
        <div>
          <h1 className="text-3xl font-bold text-foreground">
            {isEdit ? 'Editar Propietario' : 'Nuevo Propietario'}
          </h1>
          <p className="text-muted-foreground mt-1">Complete la información del propietario</p>
        </div>
      </div>

      <form onSubmit={handleSubmit(onSubmit)}>
        <Card>
          <CardHeader>
            <CardTitle>Información del Propietario</CardTitle>
          </CardHeader>
          <CardContent className="space-y-6">
            {/* Upload de imagen */}
            <div className="space-y-2">
              <Label>Foto del Propietario</Label>
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
                <Label htmlFor="nombre">Nombre Completo *</Label>
                <Input
                  id="nombre"
                  {...register('nombre')}
                  placeholder="Nombre completo del propietario"
                />
                {errors.nombre && (
                  <p className="text-sm text-destructive">{errors.nombre.message}</p>
                )}
              </div>

              <div className="space-y-2">
                <Label htmlFor="documento">Documento</Label>
                <Input
                  id="documento"
                  {...register('documento')}
                  placeholder="Número de documento"
                />
                {errors.documento && (
                  <p className="text-sm text-destructive">{errors.documento.message}</p>
                )}
              </div>

              <div className="space-y-2">
                <Label htmlFor="email">Email</Label>
                <Input
                  id="email"
                  type="email"
                  {...register('email')}
                  placeholder="correo@ejemplo.com"
                />
                {errors.email && (
                  <p className="text-sm text-destructive">{errors.email.message}</p>
                )}
              </div>

              <div className="space-y-2">
                <Label htmlFor="telefono">Teléfono</Label>
                <Input
                  id="telefono"
                  {...register('telefono')}
                  placeholder="Número de teléfono"
                />
              </div>
            </div>

            <div className="space-y-2">
              <Label htmlFor="direccion">Dirección</Label>
              <Textarea
                id="direccion"
                {...register('direccion')}
                placeholder="Dirección completa"
                rows={3}
              />
            </div>

            <div className="flex justify-end gap-3 pt-4">
              <Button type="button" variant="outline" onClick={() => navigate('/propietarios')}>
                Cancelar
              </Button>
              <Button type="submit" className="gap-2" disabled={isLoading || isLoadingData}>
                <Save className="h-4 w-4" />
                {isLoading ? 'Guardando...' : isEdit ? 'Actualizar' : 'Guardar'}
              </Button>
            </div>
          </CardContent>
        </Card>
      </form>
    </div>
  );
}
