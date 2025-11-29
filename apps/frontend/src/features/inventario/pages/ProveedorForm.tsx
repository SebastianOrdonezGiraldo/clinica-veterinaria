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
import { Switch } from '@shared/components/ui/switch';
import { useProveedor, useCreateProveedor, useUpdateProveedor } from '../hooks/useInventario';
import { LoadingCards } from '@shared/components/common/LoadingCards';

const proveedorSchema = z.object({
  nombre: z.string().min(1, 'El nombre es requerido').max(200, 'El nombre no puede exceder 200 caracteres'),
  ruc: z.string().max(50, 'El RUC no puede exceder 50 caracteres').optional().or(z.literal('')),
  email: z.string().email('Email inválido').max(100, 'El email no puede exceder 100 caracteres').optional().or(z.literal('')),
  telefono: z.string().max(20, 'El teléfono no puede exceder 20 caracteres').optional().or(z.literal('')),
  direccion: z.string().max(500, 'La dirección no puede exceder 500 caracteres').optional().or(z.literal('')),
  notas: z.string().max(1000, 'Las notas no pueden exceder 1000 caracteres').optional().or(z.literal('')),
  activo: z.boolean().default(true),
});

type ProveedorFormData = z.infer<typeof proveedorSchema>;

export default function ProveedorForm() {
  const navigate = useNavigate();
  const { id } = useParams<{ id: string }>();
  const isEditing = !!id;
  const proveedorId = id ? parseInt(id) : null;

  const { data: proveedor, isLoading: isLoadingProveedor } = useProveedor(proveedorId);
  const { mutate: createProveedor, isPending: isCreating } = useCreateProveedor();
  const { mutate: updateProveedor, isPending: isUpdating } = useUpdateProveedor();

  const form = useForm<ProveedorFormData>({
    resolver: zodResolver(proveedorSchema),
    defaultValues: {
      nombre: '',
      ruc: '',
      email: '',
      telefono: '',
      direccion: '',
      notas: '',
      activo: true,
    },
  });

  useEffect(() => {
    if (proveedor) {
      form.reset({
        nombre: proveedor.nombre,
        ruc: proveedor.ruc || '',
        email: proveedor.email || '',
        telefono: proveedor.telefono || '',
        direccion: proveedor.direccion || '',
        notas: proveedor.notas || '',
        activo: proveedor.activo,
      });
    }
  }, [proveedor, form]);

  const onSubmit = (data: ProveedorFormData) => {
    // Limpiar campos vacíos
    const cleanData = {
      ...data,
      ruc: data.ruc || undefined,
      email: data.email || undefined,
      telefono: data.telefono || undefined,
      direccion: data.direccion || undefined,
      notas: data.notas || undefined,
    };

    if (isEditing && proveedorId) {
      updateProveedor(
        { id: proveedorId, data: cleanData },
        {
          onSuccess: () => navigate('/inventario/proveedores'),
        }
      );
    } else {
      createProveedor(cleanData, {
        onSuccess: () => navigate('/inventario/proveedores'),
      });
    }
  };

  if (isEditing && isLoadingProveedor) {
    return <LoadingCards count={1} />;
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center gap-4">
        <Button variant="ghost" size="icon" onClick={() => navigate('/inventario/proveedores')}>
          <ArrowLeft className="h-4 w-4" />
        </Button>
        <div>
          <h1 className="text-3xl font-bold text-foreground">
            {isEditing ? 'Editar Proveedor' : 'Nuevo Proveedor'}
          </h1>
          <p className="text-muted-foreground mt-1">
            {isEditing ? 'Modifica los datos del proveedor' : 'Registra un nuevo proveedor'}
          </p>
        </div>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Información del Proveedor</CardTitle>
        </CardHeader>
        <CardContent>
          <Form {...form}>
            <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-6">
              <FormField
                control={form.control}
                name="nombre"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Nombre o Razón Social *</FormLabel>
                    <FormControl>
                      <Input placeholder="Ej: Proveedor S.A." {...field} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />

              <div className="grid gap-4 md:grid-cols-2">
                <FormField
                  control={form.control}
                  name="ruc"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>RUC/NIT</FormLabel>
                      <FormControl>
                        <Input placeholder="12345678901" {...field} />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />

                <FormField
                  control={form.control}
                  name="email"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>Email</FormLabel>
                      <FormControl>
                        <Input type="email" placeholder="proveedor@ejemplo.com" {...field} />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />
              </div>

              <div className="grid gap-4 md:grid-cols-2">
                <FormField
                  control={form.control}
                  name="telefono"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>Teléfono</FormLabel>
                      <FormControl>
                        <Input placeholder="1234567890" {...field} />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />

                <FormField
                  control={form.control}
                  name="direccion"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>Dirección</FormLabel>
                      <FormControl>
                        <Input placeholder="Calle, número, ciudad" {...field} />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />
              </div>

              <FormField
                control={form.control}
                name="notas"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Notas</FormLabel>
                    <FormControl>
                      <Textarea
                        placeholder="Información adicional sobre el proveedor..."
                        className="resize-none"
                        rows={3}
                        {...field}
                      />
                    </FormControl>
                    <FormDescription>
                      Términos de pago, condiciones especiales, etc.
                    </FormDescription>
                    <FormMessage />
                  </FormItem>
                )}
              />

              {isEditing && (
                <FormField
                  control={form.control}
                  name="activo"
                  render={({ field }) => (
                    <FormItem className="flex flex-row items-center justify-between rounded-lg border p-4">
                      <div className="space-y-0.5">
                        <FormLabel className="text-base">Estado</FormLabel>
                        <FormDescription>
                          Los proveedores inactivos no se mostrarán en listados
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
              )}

              <div className="flex justify-end gap-4">
                <Button
                  type="button"
                  variant="outline"
                  onClick={() => navigate('/inventario/proveedores')}
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

