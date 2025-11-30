import { useState, useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { ArrowLeft, Plus, Trash2, Save } from 'lucide-react';
import { useForm, useFieldArray } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';
import { Button } from '@shared/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@shared/components/ui/card';
import { Input } from '@shared/components/ui/input';
import { Label } from '@shared/components/ui/label';
import { Textarea } from '@shared/components/ui/textarea';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@shared/components/ui/select';
import { facturaService, ItemFactura } from '../services/facturaService';
import { propietarioService } from '@features/propietarios/services/propietarioService';
import { consultaService } from '@features/historias/services/consultaService';
import { Propietario, Consulta } from '@core/types';
import { toast } from 'sonner';
import { useLogger } from '@shared/hooks/useLogger';
import { useApiError } from '@shared/hooks/useApiError';

const facturaSchema = z.object({
  propietarioId: z.string().min(1, 'El propietario es requerido'),
  consultaId: z.string().optional(),
  fechaEmision: z.string().min(1, 'La fecha es requerida'),
  fechaVencimiento: z.string().optional(),
  descuento: z.number().min(0).optional(),
  impuesto: z.number().min(0).optional(),
  observaciones: z.string().optional(),
  items: z.array(z.object({
    descripcion: z.string().min(1, 'La descripción es requerida'),
    tipoItem: z.string().optional(),
    codigoProducto: z.string().optional(),
    cantidad: z.number().positive('La cantidad debe ser positiva'),
    precioUnitario: z.number().positive('El precio unitario debe ser positivo'),
    descuento: z.number().min(0).optional(),
  })).min(1, 'Debe agregar al menos un item'),
});

type FacturaFormData = z.infer<typeof facturaSchema>;

export default function FacturaForm() {
  const logger = useLogger('FacturaForm');
  const { handleError, showSuccess } = useApiError();
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const consultaIdFromUrl = searchParams.get('consultaId');

  const [propietarios, setPropietarios] = useState<Propietario[]>([]);
  const [consultas, setConsultas] = useState<Consulta[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [isLoadingData, setIsLoadingData] = useState(true);

  const { register, handleSubmit, formState: { errors }, control, watch, setValue } = useForm<FacturaFormData>({
    resolver: zodResolver(facturaSchema),
    defaultValues: {
      fechaEmision: new Date().toISOString().split('T')[0],
      items: [{ descripcion: '', cantidad: 1, precioUnitario: 0 }],
    },
  });

  const { fields, append, remove } = useFieldArray({
    control,
    name: 'items',
  });

  const propietarioId = watch('propietarioId');
  const consultaId = watch('consultaId');

  useEffect(() => {
    loadInitialData();
  }, []);

  useEffect(() => {
    if (consultaIdFromUrl) {
      setValue('consultaId', consultaIdFromUrl);
      loadConsultaData(consultaIdFromUrl);
    }
  }, [consultaIdFromUrl, setValue]);

  useEffect(() => {
    if (consultaId) {
      loadConsultaData(consultaId);
    }
  }, [consultaId]);

  const loadInitialData = async () => {
    try {
      setIsLoadingData(true);
      const [propietariosData, consultasData] = await Promise.all([
        propietarioService.getAll(),
        consultaService.getAll(),
      ]);
      setPropietarios(propietariosData);
      setConsultas(consultasData);
    } catch (error: any) {
      logger.error('Error al cargar datos del formulario', error);
      handleError(error, 'Error al cargar los datos');
    } finally {
      setIsLoadingData(false);
    }
  };

  const loadConsultaData = async (consultaId: string) => {
    try {
      const consulta = await consultaService.getById(consultaId);
      if (consulta.propietarioId) {
        setValue('propietarioId', consulta.propietarioId);
      }
      // Agregar item de consulta si no existe
      const items = watch('items');
      if (items.length === 1 && items[0].descripcion === '') {
        setValue('items.0.descripcion', `Consulta médica - ${consulta.diagnostico || 'Consulta general'}`);
        setValue('items.0.cantidad', 1);
        setValue('items.0.precioUnitario', 50000); // Precio por defecto
        setValue('items.0.tipoItem', 'SERVICIO');
      }
    } catch (error: any) {
      logger.warn('Error al cargar datos de consulta', error);
    }
  };

  const onSubmit = async (data: FacturaFormData) => {
    try {
      setIsLoading(true);
      let factura;

      if (data.consultaId) {
        // Crear desde consulta
        const itemsAdicionales = data.items.slice(1).map(item => ({
          descripcion: item.descripcion,
          tipoItem: item.tipoItem,
          codigoProducto: item.codigoProducto,
          cantidad: item.cantidad,
          precioUnitario: item.precioUnitario,
          descuento: item.descuento,
        }));
        factura = await facturaService.createFromConsulta(data.consultaId, itemsAdicionales);
      } else {
        // Crear factura normal
        factura = await facturaService.create({
          propietarioId: data.propietarioId,
          fechaEmision: data.fechaEmision,
          fechaVencimiento: data.fechaVencimiento,
          descuento: data.descuento,
          impuesto: data.impuesto,
          observaciones: data.observaciones,
          items: data.items.map(item => ({
            descripcion: item.descripcion,
            tipoItem: item.tipoItem,
            codigoProducto: item.codigoProducto,
            cantidad: item.cantidad,
            precioUnitario: item.precioUnitario,
            descuento: item.descuento,
          })),
        });
      }

      showSuccess('Factura creada exitosamente');
      navigate(`/facturacion/${factura.id}`);
    } catch (error: any) {
      logger.error('Error al crear factura', error, {
        action: 'createFactura',
      });
      handleError(error, 'Error al crear la factura');
    } finally {
      setIsLoading(false);
    }
  };

  const calcularTotal = () => {
    const items = watch('items');
    const subtotal = items.reduce((sum, item) => {
      const itemTotal = item.cantidad * item.precioUnitario - (item.descuento || 0);
      return sum + itemTotal;
    }, 0);
    const descuento = watch('descuento') || 0;
    const impuesto = watch('impuesto') || 0;
    return subtotal - descuento + impuesto;
  };

  const formatearMoneda = (valor: number) => {
    return new Intl.NumberFormat('es-CO', {
      style: 'currency',
      currency: 'COP',
      minimumFractionDigits: 0,
    }).format(valor);
  };

  if (isLoadingData) {
    return (
      <div className="text-center py-12">
        <p className="text-muted-foreground">Cargando datos...</p>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center gap-4">
        <Button variant="ghost" size="icon" onClick={() => navigate('/facturacion')}>
          <ArrowLeft className="h-4 w-4" />
        </Button>
        <div>
          <h1 className="text-3xl font-bold text-foreground">Nueva Factura</h1>
          <p className="text-muted-foreground mt-1">Crear una nueva factura</p>
        </div>
      </div>

      <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
        <Card>
          <CardHeader>
            <CardTitle>Información General</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="grid gap-4 md:grid-cols-2">
              <div>
                <Label>Propietario *</Label>
                <Select
                  value={propietarioId || ''}
                  onValueChange={(value) => setValue('propietarioId', value)}
                >
                  <SelectTrigger>
                    <SelectValue placeholder="Seleccionar propietario" />
                  </SelectTrigger>
                  <SelectContent>
                    {propietarios.map(prop => (
                      <SelectItem key={prop.id} value={prop.id}>
                        {prop.nombre}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
                {errors.propietarioId && (
                  <p className="text-sm text-destructive mt-1">{errors.propietarioId.message}</p>
                )}
              </div>

              <div>
                <Label>Consulta (Opcional)</Label>
                <Select
                  value={consultaId || ''}
                  onValueChange={(value) => setValue('consultaId', value)}
                >
                  <SelectTrigger>
                    <SelectValue placeholder="Seleccionar consulta" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="">Ninguna</SelectItem>
                    {consultas.map(consulta => (
                      <SelectItem key={consulta.id} value={consulta.id}>
                        {new Date(consulta.fecha).toLocaleDateString('es-ES')} - {consulta.pacienteNombre}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>

              <div>
                <Label>Fecha de Emisión *</Label>
                <Input
                  type="date"
                  {...register('fechaEmision')}
                />
                {errors.fechaEmision && (
                  <p className="text-sm text-destructive mt-1">{errors.fechaEmision.message}</p>
                )}
              </div>

              <div>
                <Label>Fecha de Vencimiento</Label>
                <Input
                  type="date"
                  {...register('fechaVencimiento')}
                />
              </div>

              <div>
                <Label>Descuento</Label>
                <Input
                  type="number"
                  step="0.01"
                  {...register('descuento', { valueAsNumber: true })}
                  placeholder="0.00"
                />
              </div>

              <div>
                <Label>Impuesto</Label>
                <Input
                  type="number"
                  step="0.01"
                  {...register('impuesto', { valueAsNumber: true })}
                  placeholder="0.00"
                />
              </div>
            </div>

            <div>
              <Label>Observaciones</Label>
              <Textarea
                {...register('observaciones')}
                placeholder="Observaciones adicionales..."
                rows={3}
              />
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <div className="flex items-center justify-between">
              <CardTitle>Items</CardTitle>
              <Button
                type="button"
                variant="outline"
                size="sm"
                onClick={() => append({ descripcion: '', cantidad: 1, precioUnitario: 0 })}
              >
                <Plus className="h-4 w-4 mr-2" />
                Agregar Item
              </Button>
            </div>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              {fields.map((field, index) => (
                <div key={field.id} className="p-4 border rounded-lg space-y-4">
                  <div className="flex items-center justify-between">
                    <h4 className="font-medium">Item {index + 1}</h4>
                    {fields.length > 1 && (
                      <Button
                        type="button"
                        variant="ghost"
                        size="sm"
                        onClick={() => remove(index)}
                      >
                        <Trash2 className="h-4 w-4 text-destructive" />
                      </Button>
                    )}
                  </div>
                  <div className="grid gap-4 md:grid-cols-2">
                    <div className="md:col-span-2">
                      <Label>Descripción *</Label>
                      <Input
                        {...register(`items.${index}.descripcion`)}
                        placeholder="Descripción del item"
                      />
                      {errors.items?.[index]?.descripcion && (
                        <p className="text-sm text-destructive mt-1">
                          {errors.items[index]?.descripcion?.message}
                        </p>
                      )}
                    </div>
                    <div>
                      <Label>Tipo</Label>
                      <Select
                        onValueChange={(value) => setValue(`items.${index}.tipoItem`, value)}
                      >
                        <SelectTrigger>
                          <SelectValue placeholder="Seleccionar tipo" />
                        </SelectTrigger>
                        <SelectContent>
                          <SelectItem value="SERVICIO">Servicio</SelectItem>
                          <SelectItem value="MEDICAMENTO">Medicamento</SelectItem>
                          <SelectItem value="PROCEDIMIENTO">Procedimiento</SelectItem>
                          <SelectItem value="OTRO">Otro</SelectItem>
                        </SelectContent>
                      </Select>
                    </div>
                    <div>
                      <Label>Código Producto</Label>
                      <Input
                        {...register(`items.${index}.codigoProducto`)}
                        placeholder="Código del producto"
                      />
                    </div>
                    <div>
                      <Label>Cantidad *</Label>
                      <Input
                        type="number"
                        step="0.01"
                        {...register(`items.${index}.cantidad`, { valueAsNumber: true })}
                        placeholder="1"
                      />
                      {errors.items?.[index]?.cantidad && (
                        <p className="text-sm text-destructive mt-1">
                          {errors.items[index]?.cantidad?.message}
                        </p>
                      )}
                    </div>
                    <div>
                      <Label>Precio Unitario *</Label>
                      <Input
                        type="number"
                        step="0.01"
                        {...register(`items.${index}.precioUnitario`, { valueAsNumber: true })}
                        placeholder="0.00"
                      />
                      {errors.items?.[index]?.precioUnitario && (
                        <p className="text-sm text-destructive mt-1">
                          {errors.items[index]?.precioUnitario?.message}
                        </p>
                      )}
                    </div>
                    <div>
                      <Label>Descuento</Label>
                      <Input
                        type="number"
                        step="0.01"
                        {...register(`items.${index}.descuento`, { valueAsNumber: true })}
                        placeholder="0.00"
                      />
                    </div>
                  </div>
                </div>
              ))}
              {errors.items && (
                <p className="text-sm text-destructive">{errors.items.message}</p>
              )}
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Resumen</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="space-y-2">
              <div className="flex justify-between">
                <span className="text-muted-foreground">Subtotal:</span>
                <span className="font-medium">
                  {formatearMoneda(
                    watch('items').reduce((sum, item) => {
                      const itemTotal = item.cantidad * item.precioUnitario - (item.descuento || 0);
                      return sum + itemTotal;
                    }, 0)
                  )}
                </span>
              </div>
              {watch('descuento') && watch('descuento')! > 0 && (
                <div className="flex justify-between">
                  <span className="text-muted-foreground">Descuento:</span>
                  <span className="font-medium text-destructive">
                    -{formatearMoneda(watch('descuento')!)}
                  </span>
                </div>
              )}
              {watch('impuesto') && watch('impuesto')! > 0 && (
                <div className="flex justify-between">
                  <span className="text-muted-foreground">Impuesto:</span>
                  <span className="font-medium">{formatearMoneda(watch('impuesto')!)}</span>
                </div>
              )}
              <div className="flex justify-between text-lg font-bold border-t pt-2">
                <span>Total:</span>
                <span>{formatearMoneda(calcularTotal())}</span>
              </div>
            </div>
          </CardContent>
        </Card>

        <div className="flex justify-end gap-2">
          <Button type="button" variant="outline" onClick={() => navigate('/facturacion')}>
            Cancelar
          </Button>
          <Button type="submit" disabled={isLoading}>
            <Save className="h-4 w-4 mr-2" />
            {isLoading ? 'Guardando...' : 'Guardar Factura'}
          </Button>
        </div>
      </form>
    </div>
  );
}

