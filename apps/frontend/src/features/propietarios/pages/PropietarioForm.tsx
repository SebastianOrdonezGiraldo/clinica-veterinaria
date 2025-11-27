import { useEffect, useState } from 'react';
import { useNavigate, useParams, useLocation } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';
import { ArrowLeft, Save } from 'lucide-react';
import { Button } from '@shared/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@shared/components/ui/card';
import { Input } from '@shared/components/ui/input';
import { Label } from '@shared/components/ui/label';
import { Textarea } from '@shared/components/ui/textarea';
import { Skeleton } from '@shared/components/ui/skeleton';
import { AlertDialog, AlertDialogAction, AlertDialogCancel, AlertDialogContent, AlertDialogDescription, AlertDialogFooter, AlertDialogHeader, AlertDialogTitle } from '@shared/components/ui/alert-dialog';
import { toast } from 'sonner';
import { propietarioService } from '@features/propietarios/services/propietarioService';
import { useLogger } from '@shared/hooks/useLogger';

const propietarioSchema = z.object({
  nombre: z.string().min(1, 'Nombre es requerido').max(100),
  documento: z.string().max(20).optional(),
  email: z.string().email('Email inválido').min(1, 'El email es requerido').max(100),
  telefono: z.string().max(20).optional(),
  direccion: z.string().max(200).optional(),
});

type PropietarioFormData = z.infer<typeof propietarioSchema>;

export default function PropietarioForm() {
  const logger = useLogger('PropietarioForm');
  const { id } = useParams();
  const navigate = useNavigate();
  const location = useLocation();
  const isEdit = !!id;

  const { register, handleSubmit, formState: { errors, isDirty }, reset } = useForm<PropietarioFormData>({
    resolver: zodResolver(propietarioSchema),
  });

  const [isLoading, setIsLoading] = useState(false);
  const [isLoadingData, setIsLoadingData] = useState(isEdit);
  const [showCancelDialog, setShowCancelDialog] = useState(false);

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
      logger.error('Error al cargar datos del formulario de propietario', error, {
        action: 'loadData',
        propietarioId: id,
        isEdit,
      });
      toast.error('Error al cargar los datos del propietario');
    } finally {
      setIsLoadingData(false);
    }
  };

  const handleCancel = () => {
    if (isDirty) {
      setShowCancelDialog(true);
    } else {
      navigate('/propietarios');
    }
  };

  const confirmCancel = () => {
    setShowCancelDialog(false);
    navigate('/propietarios');
  };

  const onSubmit = async (data: PropietarioFormData) => {
    try {
      setIsLoading(true);
      
      // Preparar datos para enviar (convertir strings vacíos a undefined para campos opcionales)
      const propietarioData = {
        nombre: data.nombre,
        documento: data.documento || undefined,
        email: data.email, // Email es requerido
        telefono: data.telefono || undefined,
        direccion: data.direccion || undefined,
      };

      if (isEdit && id) {
        await propietarioService.update(id, propietarioData);
        toast.success('Propietario actualizado exitosamente');
        navigate('/propietarios');
      } else {
        const nuevoPropietario = await propietarioService.create(propietarioData);
        toast.success('Propietario registrado exitosamente');
        
        // Si viene de otra página (ej: formulario de cita), regresar allí con el ID del propietario
        const state = location.state as { returnTo?: string } | null;
        if (state?.returnTo) {
          navigate(state.returnTo, {
            state: { propietarioId: nuevoPropietario.id }
          });
        } else {
          navigate('/propietarios');
        }
      }
    } catch (error: any) {
      logger.error('Error al guardar propietario', error, {
        action: isEdit ? 'updatePropietario' : 'createPropietario',
        propietarioId: id,
      });
      toast.error(error.response?.data?.message || `Error al ${isEdit ? 'actualizar' : 'registrar'} el propietario`);
    } finally {
      setIsLoading(false);
    }
  };

  if (isLoadingData) {
    return (
      <div className="space-y-6">
        <div className="flex items-center gap-4">
          <Skeleton className="h-10 w-10 rounded-md" />
          <div className="space-y-2">
            <Skeleton className="h-8 w-64" />
            <Skeleton className="h-4 w-48" />
          </div>
        </div>
        <Card>
          <CardHeader>
            <Skeleton className="h-6 w-48" />
          </CardHeader>
          <CardContent className="space-y-6">
            <div className="grid gap-4 md:grid-cols-2">
              <div className="space-y-2">
                <Skeleton className="h-4 w-24" />
                <Skeleton className="h-10 w-full" />
              </div>
              <div className="space-y-2">
                <Skeleton className="h-4 w-24" />
                <Skeleton className="h-10 w-full" />
              </div>
              <div className="space-y-2">
                <Skeleton className="h-4 w-24" />
                <Skeleton className="h-10 w-full" />
              </div>
              <div className="space-y-2">
                <Skeleton className="h-4 w-24" />
                <Skeleton className="h-10 w-full" />
              </div>
            </div>
            <div className="space-y-2">
              <Skeleton className="h-4 w-24" />
              <Skeleton className="h-24 w-full" />
            </div>
            <div className="flex justify-end gap-3">
              <Skeleton className="h-10 w-24" />
              <Skeleton className="h-10 w-32" />
            </div>
          </CardContent>
        </Card>
      </div>
    );
  }

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
                <Label htmlFor="email">Email *</Label>
                <Input
                  id="email"
                  type="email"
                  {...register('email')}
                  placeholder="correo@ejemplo.com"
                  required
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
              <Button type="button" variant="outline" onClick={handleCancel} disabled={isLoading}>
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

      <AlertDialog open={showCancelDialog} onOpenChange={setShowCancelDialog}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>¿Descartar cambios?</AlertDialogTitle>
            <AlertDialogDescription>
              Tienes cambios sin guardar. Si cancelas, se perderán todos los cambios realizados.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel onClick={() => setShowCancelDialog(false)}>
              Continuar editando
            </AlertDialogCancel>
            <AlertDialogAction onClick={confirmCancel} className="bg-destructive text-destructive-foreground hover:bg-destructive/90">
              Descartar cambios
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  );
}
