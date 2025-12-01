import { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { ArrowLeft, DollarSign, Download, Calendar, User, FileText, Plus, Trash2 } from 'lucide-react';
import { Button } from '@shared/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@shared/components/ui/card';
import { Badge } from '@shared/components/ui/badge';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@shared/components/ui/table';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from '@shared/components/ui/dialog';
import { Input } from '@shared/components/ui/input';
import { Label } from '@shared/components/ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@shared/components/ui/select';
import { Textarea } from '@shared/components/ui/textarea';
import { format } from 'date-fns';
import { es } from 'date-fns/locale';
import { facturaService, Factura, Pago } from '../services/facturaService';
import { toast } from 'sonner';
import { useLogger } from '@shared/hooks/useLogger';
import { useApiError } from '@shared/hooks/useApiError';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';

const pagoSchema = z.object({
  monto: z.number().positive('El monto debe ser positivo'),
  fechaPago: z.string().min(1, 'La fecha es requerida'),
  metodoPago: z.string().optional(),
  referencia: z.string().optional(),
  observaciones: z.string().optional(),
});

type PagoFormData = z.infer<typeof pagoSchema>;

const estadoColors = {
  PENDIENTE: 'bg-status-pending/10 text-status-pending border-status-pending/20',
  PARCIAL: 'bg-yellow-500/10 text-yellow-600 border-yellow-500/20',
  PAGADA: 'bg-status-completed/10 text-status-completed border-status-completed/20',
  CANCELADA: 'bg-status-cancelled/10 text-status-cancelled border-status-cancelled/20',
  VENCIDA: 'bg-red-500/10 text-red-600 border-red-500/20',
};

const estadoLabels = {
  PENDIENTE: 'Pendiente',
  PARCIAL: 'Pago Parcial',
  PAGADA: 'Pagada',
  CANCELADA: 'Cancelada',
  VENCIDA: 'Vencida',
};

export default function FacturaDetalle() {
  const logger = useLogger('FacturaDetalle');
  const { handleError, showSuccess } = useApiError();
  const navigate = useNavigate();
  const { id } = useParams<{ id: string }>();

  const [factura, setFactura] = useState<Factura | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isPagoDialogOpen, setIsPagoDialogOpen] = useState(false);
  const [isLoadingPago, setIsLoadingPago] = useState(false);

  const { register, handleSubmit, formState: { errors }, reset, setValue } = useForm<PagoFormData>({
    resolver: zodResolver(pagoSchema),
    defaultValues: {
      fechaPago: new Date().toISOString().slice(0, 16),
      metodoPago: 'EFECTIVO',
    },
  });

  useEffect(() => {
    if (id) {
      loadFactura();
    }
  }, [id]);

  const loadFactura = async () => {
    if (!id) return;
    try {
      setIsLoading(true);
      const data = await facturaService.getById(id);
      setFactura(data);
    } catch (error: any) {
      logger.error('Error al cargar factura', error, {
        action: 'loadFactura',
        facturaId: id,
      });
      handleError(error, 'Error al cargar la factura');
    } finally {
      setIsLoading(false);
    }
  };

  const onSubmitPago = async (data: PagoFormData) => {
    if (!id) return;
    try {
      setIsLoadingPago(true);
      await facturaService.registrarPago(id, {
        monto: data.monto,
        fechaPago: new Date(data.fechaPago).toISOString(),
        metodoPago: data.metodoPago,
        referencia: data.referencia,
        observaciones: data.observaciones,
      });
      showSuccess('Pago registrado exitosamente');
      setIsPagoDialogOpen(false);
      reset();
      await loadFactura();
    } catch (error: any) {
      handleError(error, 'Error al registrar el pago');
    } finally {
      setIsLoadingPago(false);
    }
  };

  const handleCancelar = async () => {
    if (!id || !factura) return;
    if (!confirm('¿Estás seguro de cancelar esta factura?')) return;

    try {
      await facturaService.cancel(id);
      showSuccess('Factura cancelada exitosamente');
      await loadFactura();
    } catch (error: any) {
      handleError(error, 'Error al cancelar la factura');
    }
  };

  const handleDownloadPdf = async () => {
    if (!id) return;
    try {
      const blob = await facturaService.downloadPdf(id);
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = `factura-${factura?.numeroFactura || id}.pdf`;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      window.URL.revokeObjectURL(url);
      showSuccess('PDF descargado exitosamente');
    } catch (error: any) {
      handleError(error, 'Error al descargar el PDF');
    }
  };

  const formatearMoneda = (valor: number) => {
    return new Intl.NumberFormat('es-CO', {
      style: 'currency',
      currency: 'COP',
      minimumFractionDigits: 0,
    }).format(valor);
  };

  if (isLoading) {
    return (
      <div className="text-center py-12">
        <p className="text-muted-foreground">Cargando factura...</p>
      </div>
    );
  }

  if (!factura) {
    return (
      <div className="text-center py-12">
        <h3 className="text-lg font-medium mb-2">Factura no encontrada</h3>
        <Button onClick={() => navigate('/facturacion')} variant="outline">
          Volver a Facturas
        </Button>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center gap-4">
        <Button variant="ghost" size="icon" onClick={() => navigate('/facturacion')}>
          <ArrowLeft className="h-4 w-4" />
        </Button>
        <div className="flex-1">
          <h1 className="text-3xl font-bold text-foreground">Factura {factura.numeroFactura}</h1>
          <p className="text-muted-foreground mt-1">
            {format(new Date(factura.fechaEmision), 'PPP', { locale: es })}
          </p>
        </div>
        <div className="flex gap-2">
          {factura.estado !== 'PAGADA' && factura.estado !== 'CANCELADA' && (
            <Dialog open={isPagoDialogOpen} onOpenChange={setIsPagoDialogOpen}>
              <DialogTrigger asChild>
                <Button>
                  <DollarSign className="h-4 w-4 mr-2" />
                  Registrar Pago
                </Button>
              </DialogTrigger>
              <DialogContent>
                <DialogHeader>
                  <DialogTitle>Registrar Pago</DialogTitle>
                </DialogHeader>
                <form onSubmit={handleSubmit(onSubmitPago)} className="space-y-4">
                  <div>
                    <Label>Monto *</Label>
                    <Input
                      type="number"
                      step="0.01"
                      {...register('monto', { valueAsNumber: true })}
                      placeholder="0.00"
                    />
                    {errors.monto && (
                      <p className="text-sm text-destructive mt-1">{errors.monto.message}</p>
                    )}
                  </div>
                  <div>
                    <Label>Fecha de Pago *</Label>
                    <Input
                      type="datetime-local"
                      {...register('fechaPago')}
                    />
                    {errors.fechaPago && (
                      <p className="text-sm text-destructive mt-1">{errors.fechaPago.message}</p>
                    )}
                  </div>
                  <div>
                    <Label>Método de Pago</Label>
                    <Select onValueChange={(value) => setValue('metodoPago', value)}>
                      <SelectTrigger>
                        <SelectValue placeholder="Seleccionar método" />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="EFECTIVO">Efectivo</SelectItem>
                        <SelectItem value="TARJETA">Tarjeta</SelectItem>
                        <SelectItem value="TRANSFERENCIA">Transferencia</SelectItem>
                        <SelectItem value="CHEQUE">Cheque</SelectItem>
                      </SelectContent>
                    </Select>
                  </div>
                  <div>
                    <Label>Referencia</Label>
                    <Input
                      {...register('referencia')}
                      placeholder="Número de transacción, cheque, etc."
                    />
                  </div>
                  <div>
                    <Label>Observaciones</Label>
                    <Textarea
                      {...register('observaciones')}
                      placeholder="Observaciones adicionales..."
                      rows={3}
                    />
                  </div>
                  <div className="flex justify-end gap-2">
                    <Button type="button" variant="outline" onClick={() => setIsPagoDialogOpen(false)}>
                      Cancelar
                    </Button>
                    <Button type="submit" disabled={isLoadingPago}>
                      {isLoadingPago ? 'Registrando...' : 'Registrar Pago'}
                    </Button>
                  </div>
                </form>
              </DialogContent>
            </Dialog>
          )}
          {factura.estado !== 'PAGADA' && factura.estado !== 'CANCELADA' && (
            <Button variant="destructive" onClick={handleCancelar}>
              <Trash2 className="h-4 w-4 mr-2" />
              Cancelar Factura
            </Button>
          )}
          <Button variant="outline" onClick={handleDownloadPdf}>
            <Download className="h-4 w-4 mr-2" />
            Descargar PDF
          </Button>
        </div>
      </div>

      <div className="grid gap-6 lg:grid-cols-3">
        {/* Información principal */}
        <Card className="lg:col-span-2">
          <CardHeader>
            <div className="flex items-center justify-between">
              <CardTitle>Detalles de la Factura</CardTitle>
              <Badge className={estadoColors[factura.estado]}>
                {estadoLabels[factura.estado]}
              </Badge>
            </div>
          </CardHeader>
          <CardContent className="space-y-6">
            {/* Información del cliente */}
            <div className="grid gap-4 md:grid-cols-2">
              <div>
                <p className="text-sm text-muted-foreground mb-1">Propietario</p>
                <p className="font-medium">{factura.propietarioNombre || 'N/A'}</p>
              </div>
              <div>
                <p className="text-sm text-muted-foreground mb-1">Fecha de Emisión</p>
                <p className="font-medium">
                  {format(new Date(factura.fechaEmision), 'PPP', { locale: es })}
                </p>
              </div>
              {factura.fechaVencimiento && (
                <div>
                  <p className="text-sm text-muted-foreground mb-1">Fecha de Vencimiento</p>
                  <p className="font-medium">
                    {format(new Date(factura.fechaVencimiento), 'PPP', { locale: es })}
                  </p>
                </div>
              )}
            </div>

            {/* Items */}
            <div>
              <h3 className="font-semibold mb-3">Items</h3>
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Descripción</TableHead>
                    <TableHead className="text-right">Cantidad</TableHead>
                    <TableHead className="text-right">Precio Unit.</TableHead>
                    <TableHead className="text-right">Descuento</TableHead>
                    <TableHead className="text-right">Subtotal</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {factura.items.map((item, index) => (
                    <TableRow key={index}>
                      <TableCell>{item.descripcion}</TableCell>
                      <TableCell className="text-right">{item.cantidad}</TableCell>
                      <TableCell className="text-right">{formatearMoneda(item.precioUnitario)}</TableCell>
                      <TableCell className="text-right">
                        {item.descuento ? formatearMoneda(item.descuento) : '-'}
                      </TableCell>
                      <TableCell className="text-right font-medium">
                        {formatearMoneda(item.subtotal || item.cantidad * item.precioUnitario - (item.descuento || 0))}
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </div>

            {/* Totales */}
            <div className="border-t pt-4 space-y-2">
              <div className="flex justify-between">
                <span className="text-muted-foreground">Subtotal:</span>
                <span className="font-medium">{formatearMoneda(factura.subtotal)}</span>
              </div>
              {factura.descuento && factura.descuento > 0 && (
                <div className="flex justify-between">
                  <span className="text-muted-foreground">Descuento:</span>
                  <span className="font-medium text-destructive">-{formatearMoneda(factura.descuento)}</span>
                </div>
              )}
              {factura.impuesto && factura.impuesto > 0 && (
                <div className="flex justify-between">
                  <span className="text-muted-foreground">Impuesto:</span>
                  <span className="font-medium">{formatearMoneda(factura.impuesto)}</span>
                </div>
              )}
              <div className="flex justify-between text-lg font-bold border-t pt-2">
                <span>Total:</span>
                <span>{formatearMoneda(factura.total)}</span>
              </div>
            </div>

            {factura.observaciones && (
              <div>
                <h3 className="font-semibold mb-2">Observaciones</h3>
                <p className="text-sm text-muted-foreground">{factura.observaciones}</p>
              </div>
            )}
          </CardContent>
        </Card>

        {/* Resumen de pagos */}
        <Card>
          <CardHeader>
            <CardTitle>Pagos</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="space-y-2">
              <div className="flex justify-between">
                <span className="text-muted-foreground">Total:</span>
                <span className="font-semibold">{formatearMoneda(factura.total)}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-muted-foreground">Pagado:</span>
                <span className="font-semibold text-green-600">{formatearMoneda(factura.montoPagado)}</span>
              </div>
              <div className="flex justify-between border-t pt-2">
                <span className="text-muted-foreground">Pendiente:</span>
                <span className={`font-semibold ${factura.montoPendiente > 0 ? 'text-destructive' : 'text-green-600'}`}>
                  {formatearMoneda(factura.montoPendiente)}
                </span>
              </div>
            </div>

            {factura.pagos && factura.pagos.length > 0 && (
              <div className="mt-4">
                <h4 className="font-semibold mb-2">Historial de Pagos</h4>
                <div className="space-y-2">
                  {factura.pagos.map((pago) => (
                    <div key={pago.id} className="p-2 border rounded">
                      <div className="flex justify-between items-start mb-1">
                        <span className="font-medium">{formatearMoneda(pago.monto)}</span>
                        <span className="text-xs text-muted-foreground">
                          {format(new Date(pago.fechaPago), 'dd/MM/yyyy HH:mm')}
                        </span>
                      </div>
                      {pago.metodoPago && (
                        <p className="text-xs text-muted-foreground">Método: {pago.metodoPago}</p>
                      )}
                      {pago.referencia && (
                        <p className="text-xs text-muted-foreground">Ref: {pago.referencia}</p>
                      )}
                    </div>
                  ))}
                </div>
              </div>
            )}
          </CardContent>
        </Card>
      </div>
    </div>
  );
}

