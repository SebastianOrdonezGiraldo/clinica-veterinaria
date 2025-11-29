import { useEffect } from 'react';
import { useNavigate, useParams, useSearchParams } from 'react-router-dom';
import { useForm, Controller } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { ArrowLeft, Save, ArrowDown, ArrowUp, RefreshCw } from 'lucide-react';
import { Button } from '@shared/components/ui/button';
import { Input } from '@shared/components/ui/input';
import { Textarea } from '@shared/components/ui/textarea';
import { Card, CardContent, CardHeader, CardTitle } from '@shared/components/ui/card';
import { Form, FormControl, FormDescription, FormField, FormItem, FormLabel, FormMessage } from '@shared/components/ui/form';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@shared/components/ui/select';
import { Badge } from '@shared/components/ui/badge';
import { useProductos, useProveedores, useRegistrarEntrada, useRegistrarSalida, useRegistrarAjuste } from '../hooks/useInventario';
import { useAuth } from '@core/auth/AuthContext';
import { LoadingCards } from '@shared/components/common/LoadingCards';
import { TipoMovimiento } from '../services/inventarioService';

const movimientoBaseSchema = z.object({
  productoId: z.number().min(1, 'El producto es requerido'),
  cantidad: z.number().int('La cantidad debe ser un número entero').min(1, 'La cantidad debe ser mayor a 0'),
  precioUnitario: z.number().min(0, 'El precio unitario no puede ser negativo').optional().nullable(),
  motivo: z.string().min(1, 'El motivo es requerido').max(500, 'El motivo no puede exceder 500 caracteres'),
  notas: z.string().max(1000, 'Las notas no pueden exceder 1000 caracteres').optional().or(z.literal('')),
});

const entradaSchema = movimientoBaseSchema.extend({
  proveedorId: z.number().min(1, 'El proveedor es requerido para entradas'),
});

type MovimientoFormData = z.infer<typeof movimientoBaseSchema> & {
  proveedorId?: number;
};

export default function MovimientoForm() {
  const navigate = useNavigate();
  const { tipo } = useParams<{ tipo: string }>();
  const { user } = useAuth();
  const [searchParams] = useSearchParams();
  const productoIdParam = searchParams.get('productoId');

  const tipoMovimiento = tipo?.toUpperCase() as TipoMovimiento;
  const isEntrada = tipoMovimiento === 'ENTRADA';
  const isSalida = tipoMovimiento === 'SALIDA';
  const isAjuste = tipoMovimiento === 'AJUSTE';

  const { data: productos = [] } = useProductos();
  const { data: proveedores = [] } = useProveedores();
  const { mutate: registrarEntrada, isPending: isCreatingEntrada } = useRegistrarEntrada();
  const { mutate: registrarSalida, isPending: isCreatingSalida } = useRegistrarSalida();
  const { mutate: registrarAjuste, isPending: isCreatingAjuste } = useRegistrarAjuste();

  const schema = isEntrada ? entradaSchema : movimientoBaseSchema;
  const isPending = isCreatingEntrada || isCreatingSalida || isCreatingAjuste;

  const form = useForm<MovimientoFormData>({
    resolver: zodResolver(schema),
    defaultValues: {
      productoId: productoIdParam ? parseInt(productoIdParam) : 0,
      cantidad: 0,
      precioUnitario: undefined,
      motivo: '',
      notas: '',
      proveedorId: undefined,
    },
  });

  useEffect(() => {
    if (productoIdParam) {
      form.setValue('productoId', parseInt(productoIdParam));
    }
  }, [productoIdParam, form]);

  const onSubmit = (data: MovimientoFormData) => {
    if (!user?.id) {
      return;
    }

    const movimientoData = {
      ...data,
      tipo: tipoMovimiento,
      usuarioId: parseInt(user.id),
      precioUnitario: data.precioUnitario ?? undefined,
      notas: data.notas || undefined,
    };

    if (isEntrada) {
      registrarEntrada(movimientoData as any, {
        onSuccess: () => navigate('/inventario/movimientos'),
      });
    } else if (isSalida) {
      registrarSalida(movimientoData, {
        onSuccess: () => navigate('/inventario/movimientos'),
      });
    } else if (isAjuste) {
      registrarAjuste(movimientoData, {
        onSuccess: () => navigate('/inventario/movimientos'),
      });
    }
  };

  const getTipoInfo = () => {
    switch (tipoMovimiento) {
      case 'ENTRADA':
        return {
          title: 'Registrar Entrada',
          description: 'Registra productos que ingresan al inventario (compras, recepciones)',
          icon: <ArrowDown className="h-5 w-5 text-green-600" />,
          badge: <Badge className="bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200">Entrada</Badge>,
        };
      case 'SALIDA':
        return {
          title: 'Registrar Salida',
          description: 'Registra productos que salen del inventario (ventas, uso, pérdidas)',
          icon: <ArrowUp className="h-5 w-5 text-red-600" />,
          badge: <Badge className="bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200">Salida</Badge>,
        };
      case 'AJUSTE':
        return {
          title: 'Registrar Ajuste',
          description: 'Corrige el stock del producto (inventario físico, correcciones)',
          icon: <RefreshCw className="h-5 w-5 text-blue-600" />,
          badge: <Badge className="bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-200">Ajuste</Badge>,
        };
      default:
        return null;
    }
  };

  const tipoInfo = getTipoInfo();
  if (!tipoInfo) {
    return <div>Tipo de movimiento no válido</div>;
  }

  const productoSeleccionado = productos.find(p => p.id === form.watch('productoId'));

  return (
    <div className="space-y-6">
      <div className="flex items-center gap-4">
        <Button variant="ghost" size="icon" onClick={() => navigate('/inventario/movimientos')}>
          <ArrowLeft className="h-4 w-4" />
        </Button>
        <div className="flex items-center gap-3">
          {tipoInfo.icon}
          <div>
            <h1 className="text-3xl font-bold text-foreground">{tipoInfo.title}</h1>
            <p className="text-muted-foreground mt-1">{tipoInfo.description}</p>
          </div>
          {tipoInfo.badge}
        </div>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Datos del Movimiento</CardTitle>
        </CardHeader>
        <CardContent>
          <Form {...form}>
            <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-6">
              <FormField
                control={form.control}
                name="productoId"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Producto *</FormLabel>
                    <Select
                      value={field.value?.toString()}
                      onValueChange={(value) => field.onChange(parseInt(value))}
                    >
                      <FormControl>
                        <SelectTrigger>
                          <SelectValue placeholder="Seleccionar producto" />
                        </SelectTrigger>
                      </FormControl>
                      <SelectContent>
                        {productos.map((prod) => (
                          <SelectItem key={prod.id} value={prod.id.toString()}>
                            {prod.nombre} ({prod.codigo}) - Stock: {prod.stockActual} {prod.unidadMedida}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                    {productoSeleccionado && (
                      <FormDescription>
                        Stock actual: <strong>{productoSeleccionado.stockActual} {productoSeleccionado.unidadMedida}</strong>
                        {productoSeleccionado.stockMinimo !== undefined && (
                          <span className="ml-2">
                            | Mínimo: {productoSeleccionado.stockMinimo} {productoSeleccionado.unidadMedida}
                          </span>
                        )}
                      </FormDescription>
                    )}
                    <FormMessage />
                  </FormItem>
                )}
              />

              {isAjuste && (
                <div className="rounded-lg border border-blue-200 bg-blue-50 dark:bg-blue-950 p-4">
                  <p className="text-sm text-blue-800 dark:text-blue-200">
                    <strong>Nota:</strong> Para ajustes, la cantidad representa el stock final deseado.
                    El sistema calculará automáticamente la diferencia con el stock actual.
                  </p>
                </div>
              )}

              <div className="grid gap-4 md:grid-cols-2">
                <FormField
                  control={form.control}
                  name="cantidad"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>Cantidad *</FormLabel>
                      <FormControl>
                        <Input
                          type="number"
                          step="1"
                          min="1"
                          {...field}
                          value={field.value ?? ''}
                          onChange={(e) => {
                            const value = e.target.value;
                            if (value === '') {
                              field.onChange(0);
                            } else {
                              const intValue = parseInt(value, 10);
                              if (!isNaN(intValue) && intValue >= 1) {
                                field.onChange(intValue);
                              }
                            }
                          }}
                        />
                      </FormControl>
                      <FormDescription>
                        {isAjuste 
                          ? 'Stock final deseado (número entero)' 
                          : `Cantidad a ${isEntrada ? 'ingresar' : 'retirar'} (número entero)`}
                      </FormDescription>
                      <FormMessage />
                    </FormItem>
                  )}
                />

                <FormField
                  control={form.control}
                  name="precioUnitario"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>Precio Unitario</FormLabel>
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
                      <FormDescription>
                        {isEntrada ? 'Precio de compra' : isSalida ? 'Precio de venta (opcional)' : 'No aplica para ajustes'}
                      </FormDescription>
                      <FormMessage />
                    </FormItem>
                  )}
                />
              </div>

              {isEntrada && (
                <FormField
                  control={form.control}
                  name="proveedorId"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>Proveedor *</FormLabel>
                      <Select
                        value={field.value?.toString()}
                        onValueChange={(value) => field.onChange(parseInt(value))}
                      >
                        <FormControl>
                          <SelectTrigger>
                            <SelectValue placeholder="Seleccionar proveedor" />
                          </SelectTrigger>
                        </FormControl>
                        <SelectContent>
                          {proveedores.map((prov) => (
                            <SelectItem key={prov.id} value={prov.id.toString()}>
                              {prov.nombre}
                            </SelectItem>
                          ))}
                        </SelectContent>
                      </Select>
                      <FormDescription>
                        Proveedor del cual se recibió el producto
                      </FormDescription>
                      <FormMessage />
                    </FormItem>
                  )}
                />
              )}

              <FormField
                control={form.control}
                name="motivo"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Motivo *</FormLabel>
                    <FormControl>
                      <Textarea
                        placeholder={
                          isEntrada 
                            ? "Ej: Compra a proveedor X, Recepción de mercancía..."
                            : isSalida
                            ? "Ej: Venta a cliente, Uso en consulta #123, Pérdida..."
                            : "Ej: Inventario físico, Corrección de error..."
                        }
                        className="resize-none"
                        rows={3}
                        {...field}
                      />
                    </FormControl>
                    <FormDescription>
                      Razón o descripción del movimiento
                    </FormDescription>
                    <FormMessage />
                  </FormItem>
                )}
              />

              <FormField
                control={form.control}
                name="notas"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Notas Adicionales</FormLabel>
                    <FormControl>
                      <Textarea
                        placeholder="Información adicional sobre el movimiento..."
                        className="resize-none"
                        rows={2}
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
                  onClick={() => navigate('/inventario/movimientos')}
                >
                  Cancelar
                </Button>
                <Button type="submit" disabled={isPending} className="gap-2">
                  <Save className="h-4 w-4" />
                  {isPending ? 'Registrando...' : 'Registrar Movimiento'}
                </Button>
              </div>
            </form>
          </Form>
        </CardContent>
      </Card>
    </div>
  );
}

