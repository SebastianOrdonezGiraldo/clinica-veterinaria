import { useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { useForm, Controller } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { ArrowLeft, Save, Plus } from 'lucide-react';
import { Button } from '@shared/components/ui/button';
import { Input } from '@shared/components/ui/input';
import { Textarea } from '@shared/components/ui/textarea';
import { Card, CardContent, CardHeader, CardTitle } from '@shared/components/ui/card';
import { Form, FormControl, FormDescription, FormField, FormItem, FormLabel, FormMessage } from '@shared/components/ui/form';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@shared/components/ui/select';
import { Switch } from '@shared/components/ui/switch';
import { useProducto, useCreateProducto, useUpdateProducto, useCategorias } from '../hooks/useInventario';
import { LoadingCards } from '@shared/components/common/LoadingCards';

const productoSchema = z.object({
  nombre: z.string().min(1, 'El nombre es requerido').max(200, 'El nombre no puede exceder 200 caracteres'),
  codigo: z.string().min(1, 'El código es requerido').max(50, 'El código no puede exceder 50 caracteres'),
  descripcion: z.string().max(1000, 'La descripción no puede exceder 1000 caracteres').optional().or(z.literal('')),
  categoriaId: z.number().min(1, 'La categoría es requerida'),
  unidadMedida: z.string().min(1, 'La unidad de medida es requerida').max(20),
  stockActual: z.number().min(0, 'El stock actual no puede ser negativo').default(0),
  stockMinimo: z.number().min(0, 'El stock mínimo no puede ser negativo').optional().nullable(),
  stockMaximo: z.number().min(0, 'El stock máximo no puede ser negativo').optional().nullable(),
  costo: z.number().min(0, 'El costo no puede ser negativo').default(0),
  precioVenta: z.number().min(0, 'El precio de venta no puede ser negativo').optional().nullable(),
  activo: z.boolean().default(true),
});

type ProductoFormData = z.infer<typeof productoSchema>;

export default function ProductoForm() {
  const navigate = useNavigate();
  const { id } = useParams<{ id: string }>();
  const isEditing = !!id;
  const productoId = id ? parseInt(id) : null;

  const { data: producto, isLoading: isLoadingProducto } = useProducto(productoId);
  const { data: categorias = [], isLoading: isLoadingCategorias } = useCategorias();
  const { mutate: createProducto, isPending: isCreating } = useCreateProducto();
  const { mutate: updateProducto, isPending: isUpdating } = useUpdateProducto();

  const form = useForm<ProductoFormData>({
    resolver: zodResolver(productoSchema),
    defaultValues: {
      nombre: '',
      codigo: '',
      descripcion: '',
      categoriaId: 0,
      unidadMedida: 'unidad',
      stockActual: 0,
      stockMinimo: undefined,
      stockMaximo: undefined,
      costo: 0,
      precioVenta: undefined,
      activo: true,
    },
  });

  useEffect(() => {
    if (producto) {
      form.reset({
        nombre: producto.nombre,
        codigo: producto.codigo,
        descripcion: producto.descripcion || '',
        categoriaId: producto.categoriaId,
        unidadMedida: producto.unidadMedida,
        stockActual: producto.stockActual,
        stockMinimo: producto.stockMinimo ?? undefined,
        stockMaximo: producto.stockMaximo ?? undefined,
        costo: producto.costo,
        precioVenta: producto.precioVenta ?? undefined,
        activo: producto.activo,
      });
    }
  }, [producto, form]);

  const onSubmit = (data: ProductoFormData) => {
    const cleanData = {
      ...data,
      stockMinimo: data.stockMinimo ?? undefined,
      stockMaximo: data.stockMaximo ?? undefined,
      precioVenta: data.precioVenta ?? undefined,
    };

    if (isEditing && productoId) {
      updateProducto(
        { id: productoId, data: cleanData },
        {
          onSuccess: () => navigate('/inventario/productos'),
        }
      );
    } else {
      createProducto(cleanData, {
        onSuccess: () => navigate('/inventario/productos'),
      });
    }
  };

  if (isEditing && isLoadingProducto) {
    return <LoadingCards count={1} />;
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center gap-4">
        <Button variant="ghost" size="icon" onClick={() => navigate('/inventario/productos')}>
          <ArrowLeft className="h-4 w-4" />
        </Button>
        <div>
          <h1 className="text-3xl font-bold text-foreground">
            {isEditing ? 'Editar Producto' : 'Nuevo Producto'}
          </h1>
          <p className="text-muted-foreground mt-1">
            {isEditing ? 'Modifica los datos del producto' : 'Registra un nuevo producto en el inventario'}
          </p>
        </div>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Información del Producto</CardTitle>
        </CardHeader>
        <CardContent>
          <Form {...form}>
            <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-6">
              <div className="grid gap-4 md:grid-cols-2">
                <FormField
                  control={form.control}
                  name="nombre"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>Nombre *</FormLabel>
                      <FormControl>
                        <Input placeholder="Ej: Amoxicilina 500mg" {...field} />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />

                <FormField
                  control={form.control}
                  name="codigo"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>Código *</FormLabel>
                      <FormControl>
                        <Input placeholder="Ej: AMOX-500" {...field} />
                      </FormControl>
                      <FormDescription>
                        Código único del producto (SKU, código de barras, etc.)
                      </FormDescription>
                      <FormMessage />
                    </FormItem>
                  )}
                />
              </div>

              <FormField
                control={form.control}
                name="descripcion"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Descripción</FormLabel>
                    <FormControl>
                      <Textarea
                        placeholder="Descripción detallada del producto..."
                        className="resize-none"
                        rows={3}
                        {...field}
                      />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />

              <div className="grid gap-4 md:grid-cols-2">
                <FormField
                  control={form.control}
                  name="categoriaId"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>Categoría *</FormLabel>
                      {isLoadingCategorias ? (
                        <div className="space-y-2">
                          <SelectTrigger disabled>
                            <SelectValue placeholder="Cargando categorías..." />
                          </SelectTrigger>
                        </div>
                      ) : categorias.length === 0 ? (
                        <div className="space-y-2">
                          <div className="rounded-lg border border-yellow-200 bg-yellow-50 dark:bg-yellow-950 p-4">
                            <p className="text-sm text-yellow-800 dark:text-yellow-200 mb-2">
                              <strong>No hay categorías disponibles.</strong> Debes crear al menos una categoría antes de crear un producto.
                            </p>
                            <Button
                              type="button"
                              variant="outline"
                              size="sm"
                              onClick={() => navigate('/inventario/categorias/nueva')}
                              className="gap-2"
                            >
                              <Plus className="h-4 w-4" />
                              Crear Categoría
                            </Button>
                          </div>
                          <FormMessage />
                        </div>
                      ) : (
                        <>
                          <Select
                            value={field.value?.toString()}
                            onValueChange={(value) => field.onChange(parseInt(value))}
                          >
                            <FormControl>
                              <SelectTrigger>
                                <SelectValue placeholder="Seleccionar categoría" />
                              </SelectTrigger>
                            </FormControl>
                            <SelectContent>
                              {categorias.map((cat) => (
                                <SelectItem key={cat.id} value={cat.id.toString()}>
                                  {cat.nombre}
                                </SelectItem>
                              ))}
                            </SelectContent>
                          </Select>
                          <FormDescription>
                            <Button
                              type="button"
                              variant="link"
                              size="sm"
                              className="h-auto p-0 text-xs"
                              onClick={() => navigate('/inventario/categorias/nueva')}
                            >
                              + Crear nueva categoría
                            </Button>
                          </FormDescription>
                          <FormMessage />
                        </>
                      )}
                    </FormItem>
                  )}
                />

                <FormField
                  control={form.control}
                  name="unidadMedida"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>Unidad de Medida *</FormLabel>
                      <FormControl>
                        <Input placeholder="Ej: unidad, caja, frasco, kg" {...field} />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />
              </div>

              <div className="grid gap-4 md:grid-cols-3">
                <FormField
                  control={form.control}
                  name="stockActual"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>Stock Actual</FormLabel>
                      <FormControl>
                        <Input
                          type="number"
                          step="0.01"
                          min="0"
                          {...field}
                          onChange={(e) => field.onChange(parseFloat(e.target.value) || 0)}
                        />
                      </FormControl>
                      <FormDescription>
                        {isEditing ? 'Se actualiza mediante movimientos' : 'Stock inicial'}
                      </FormDescription>
                      <FormMessage />
                    </FormItem>
                  )}
                />

                <FormField
                  control={form.control}
                  name="stockMinimo"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>Stock Mínimo</FormLabel>
                      <FormControl>
                        <Input
                          type="number"
                          step="0.01"
                          min="0"
                          {...field}
                          value={field.value ?? ''}
                          onChange={(e) => field.onChange(e.target.value ? parseFloat(e.target.value) : undefined)}
                        />
                      </FormControl>
                      <FormDescription>Alerta cuando el stock cae por debajo</FormDescription>
                      <FormMessage />
                    </FormItem>
                  )}
                />

                <FormField
                  control={form.control}
                  name="stockMaximo"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>Stock Máximo</FormLabel>
                      <FormControl>
                        <Input
                          type="number"
                          step="0.01"
                          min="0"
                          {...field}
                          value={field.value ?? ''}
                          onChange={(e) => field.onChange(e.target.value ? parseFloat(e.target.value) : undefined)}
                        />
                      </FormControl>
                      <FormDescription>Nivel máximo recomendado</FormDescription>
                      <FormMessage />
                    </FormItem>
                  )}
                />
              </div>

              <div className="grid gap-4 md:grid-cols-2">
                <FormField
                  control={form.control}
                  name="costo"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>Costo *</FormLabel>
                      <FormControl>
                        <Input
                          type="number"
                          step="0.01"
                          min="0"
                          {...field}
                          onChange={(e) => field.onChange(parseFloat(e.target.value) || 0)}
                        />
                      </FormControl>
                      <FormDescription>Precio de compra</FormDescription>
                      <FormMessage />
                    </FormItem>
                  )}
                />

                <FormField
                  control={form.control}
                  name="precioVenta"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>Precio de Venta</FormLabel>
                      <FormControl>
                        <Input
                          type="number"
                          step="0.01"
                          min="0"
                          {...field}
                          value={field.value ?? ''}
                          onChange={(e) => field.onChange(e.target.value ? parseFloat(e.target.value) : undefined)}
                        />
                      </FormControl>
                      <FormDescription>Precio de venta (opcional)</FormDescription>
                      <FormMessage />
                    </FormItem>
                  )}
                />
              </div>

              {isEditing && (
                <FormField
                  control={form.control}
                  name="activo"
                  render={({ field }) => (
                    <FormItem className="flex flex-row items-center justify-between rounded-lg border p-4">
                      <div className="space-y-0.5">
                        <FormLabel className="text-base">Estado</FormLabel>
                        <FormDescription>
                          Los productos inactivos no se mostrarán en listados
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
                  onClick={() => navigate('/inventario/productos')}
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

