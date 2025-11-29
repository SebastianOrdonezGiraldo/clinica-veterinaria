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
import { useCategoria, useCreateCategoria, useUpdateCategoria } from '../hooks/useInventario';
import { LoadingCards } from '@shared/components/common/LoadingCards';

const categoriaSchema = z.object({
  nombre: z.string().min(1, 'El nombre es requerido').max(100, 'El nombre no puede exceder 100 caracteres'),
  descripcion: z.string().max(500, 'La descripción no puede exceder 500 caracteres').optional().or(z.literal('')),
  activo: z.boolean().default(true),
});

type CategoriaFormData = z.infer<typeof categoriaSchema>;

export default function CategoriaForm() {
  const navigate = useNavigate();
  const { id } = useParams<{ id: string }>();
  const isEditing = !!id;
  const categoriaId = id ? parseInt(id) : null;

  const { data: categoria, isLoading: isLoadingCategoria } = useCategoria(categoriaId);
  const { mutate: createCategoria, isPending: isCreating } = useCreateCategoria();
  const { mutate: updateCategoria, isPending: isUpdating } = useUpdateCategoria();

  const form = useForm<CategoriaFormData>({
    resolver: zodResolver(categoriaSchema),
    defaultValues: {
      nombre: '',
      descripcion: '',
      activo: true,
    },
  });

  useEffect(() => {
    if (categoria) {
      form.reset({
        nombre: categoria.nombre,
        descripcion: categoria.descripcion || '',
        activo: categoria.activo,
      });
    }
  }, [categoria, form]);

  const onSubmit = (data: CategoriaFormData) => {
    if (isEditing && categoriaId) {
      updateCategoria(
        { id: categoriaId, data },
        {
          onSuccess: () => navigate('/inventario/categorias'),
        }
      );
    } else {
      createCategoria(data, {
        onSuccess: () => navigate('/inventario/categorias'),
      });
    }
  };

  if (isEditing && isLoadingCategoria) {
    return <LoadingCards count={1} />;
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center gap-4">
        <Button variant="ghost" size="icon" onClick={() => navigate('/inventario/categorias')}>
          <ArrowLeft className="h-4 w-4" />
        </Button>
        <div>
          <h1 className="text-3xl font-bold text-foreground">
            {isEditing ? 'Editar Categoría' : 'Nueva Categoría'}
          </h1>
          <p className="text-muted-foreground mt-1">
            {isEditing ? 'Modifica los datos de la categoría' : 'Crea una nueva categoría de productos'}
          </p>
        </div>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Información de la Categoría</CardTitle>
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
                      <Input placeholder="Ej: Medicamentos, Insumos, Alimentos..." {...field} />
                    </FormControl>
                    <FormDescription>
                      Nombre único de la categoría
                    </FormDescription>
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
                        placeholder="Descripción opcional de la categoría..."
                        className="resize-none"
                        rows={3}
                        {...field}
                      />
                    </FormControl>
                    <FormDescription>
                      Información adicional sobre esta categoría
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
                          Las categorías inactivas no se mostrarán en listados
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
                  onClick={() => navigate('/inventario/categorias')}
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

