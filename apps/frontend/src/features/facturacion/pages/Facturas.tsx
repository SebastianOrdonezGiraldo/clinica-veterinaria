import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { FileText, Plus, Search, Filter, Download, DollarSign, Calendar, User } from 'lucide-react';
import { Button } from '@shared/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@shared/components/ui/card';
import { Input } from '@shared/components/ui/input';
import { Badge } from '@shared/components/ui/badge';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@shared/components/ui/select';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@shared/components/ui/table';
import { format } from 'date-fns';
import { es } from 'date-fns/locale';
import { facturaService, Factura, EstadoFactura, FacturaSearchParams } from '../services/facturaService';
import { PageResponse } from '@core/types';
import { toast } from 'sonner';
import { useLogger } from '@shared/hooks/useLogger';
import { useApiError } from '@shared/hooks/useApiError';

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

export default function Facturas() {
  const logger = useLogger('Facturas');
  const { handleError } = useApiError();
  const navigate = useNavigate();

  const [facturas, setFacturas] = useState<PageResponse<Factura> | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [currentPage, setCurrentPage] = useState(0);
  const [filtroEstado, setFiltroEstado] = useState<EstadoFactura | 'todos'>('todos');
  const [searchQuery, setSearchQuery] = useState('');

  useEffect(() => {
    loadFacturas();
  }, [currentPage, filtroEstado]);

  const loadFacturas = async () => {
    try {
      setIsLoading(true);
      const params: FacturaSearchParams = {
        page: currentPage,
        size: 20,
        sort: 'fechaEmision,desc',
        estado: filtroEstado !== 'todos' ? filtroEstado : undefined,
      };
      const result = await facturaService.getAll(params);
      setFacturas(result);
    } catch (error: any) {
      logger.error('Error al cargar facturas', error, {
        action: 'loadFacturas',
      });
      handleError(error, 'Error al cargar facturas');
    } finally {
      setIsLoading(false);
    }
  };

  const facturasFiltradas = facturas?.content.filter(factura =>
    searchQuery.trim() === '' ||
    factura.numeroFactura.toLowerCase().includes(searchQuery.toLowerCase()) ||
    factura.propietarioNombre?.toLowerCase().includes(searchQuery.toLowerCase())
  ) || [];

  const formatearMoneda = (valor: number) => {
    return new Intl.NumberFormat('es-CO', {
      style: 'currency',
      currency: 'COP',
      minimumFractionDigits: 0,
    }).format(valor);
  };

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-3xl font-bold text-foreground">Facturación</h1>
          <p className="text-muted-foreground mt-1">Gestión de facturas y pagos</p>
        </div>
        <Button onClick={() => navigate('/facturacion/nueva')}>
          <Plus className="h-4 w-4 mr-2" />
          Nueva Factura
        </Button>
      </div>

      {/* Filtros */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Filter className="h-5 w-5 text-primary" />
            Filtros
          </CardTitle>
        </CardHeader>
        <CardContent>
          <div className="grid gap-4 md:grid-cols-3">
            <div>
              <label className="text-sm font-medium mb-2 block">Búsqueda</label>
              <div className="relative">
                <Search className="absolute left-2 top-2.5 h-4 w-4 text-muted-foreground" />
                <Input
                  placeholder="Buscar por número o propietario..."
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                  className="pl-8"
                />
              </div>
            </div>
            <div>
              <label className="text-sm font-medium mb-2 block">Estado</label>
              <Select value={filtroEstado} onValueChange={(value) => {
                setFiltroEstado(value as EstadoFactura | 'todos');
                setCurrentPage(0);
              }}>
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="todos">Todos</SelectItem>
                  <SelectItem value="PENDIENTE">Pendiente</SelectItem>
                  <SelectItem value="PARCIAL">Pago Parcial</SelectItem>
                  <SelectItem value="PAGADA">Pagada</SelectItem>
                  <SelectItem value="CANCELADA">Cancelada</SelectItem>
                  <SelectItem value="VENCIDA">Vencida</SelectItem>
                </SelectContent>
              </Select>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Lista de facturas */}
      <Card>
        <CardHeader>
          <CardTitle>Facturas ({facturas?.totalElements || 0})</CardTitle>
        </CardHeader>
        <CardContent>
          {isLoading ? (
            <div className="text-center py-12">
              <p className="text-muted-foreground">Cargando facturas...</p>
            </div>
          ) : facturasFiltradas.length > 0 ? (
            <>
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Número</TableHead>
                    <TableHead>Fecha</TableHead>
                    <TableHead>Propietario</TableHead>
                    <TableHead>Total</TableHead>
                    <TableHead>Pagado</TableHead>
                    <TableHead>Pendiente</TableHead>
                    <TableHead>Estado</TableHead>
                    <TableHead className="text-right">Acciones</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {facturasFiltradas.map((factura) => (
                    <TableRow key={factura.id} className="cursor-pointer" onClick={() => navigate(`/facturacion/${factura.id}`)}>
                      <TableCell className="font-medium">{factura.numeroFactura}</TableCell>
                      <TableCell>
                        {format(new Date(factura.fechaEmision), 'PPP', { locale: es })}
                      </TableCell>
                      <TableCell>{factura.propietarioNombre || 'N/A'}</TableCell>
                      <TableCell className="font-semibold">{formatearMoneda(factura.total)}</TableCell>
                      <TableCell>{formatearMoneda(factura.montoPagado)}</TableCell>
                      <TableCell className={factura.montoPendiente > 0 ? 'text-destructive font-semibold' : ''}>
                        {formatearMoneda(factura.montoPendiente)}
                      </TableCell>
                      <TableCell>
                        <Badge className={estadoColors[factura.estado]}>
                          {estadoLabels[factura.estado]}
                        </Badge>
                      </TableCell>
                      <TableCell className="text-right">
                        <Button
                          variant="ghost"
                          size="sm"
                          onClick={(e) => {
                            e.stopPropagation();
                            navigate(`/facturacion/${factura.id}`);
                          }}
                        >
                          <FileText className="h-4 w-4" />
                        </Button>
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
              {facturas && facturas.totalPages > 1 && (
                <div className="flex items-center justify-between mt-4">
                  <p className="text-sm text-muted-foreground">
                    Página {currentPage + 1} de {facturas.totalPages}
                  </p>
                  <div className="flex gap-2">
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={() => setCurrentPage(prev => Math.max(0, prev - 1))}
                      disabled={currentPage === 0}
                    >
                      Anterior
                    </Button>
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={() => setCurrentPage(prev => Math.min(facturas.totalPages - 1, prev + 1))}
                      disabled={currentPage === facturas.totalPages - 1}
                    >
                      Siguiente
                    </Button>
                  </div>
                </div>
              )}
            </>
          ) : (
            <div className="text-center py-12">
              <FileText className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
              <h3 className="text-lg font-medium text-foreground mb-2">No hay facturas</h3>
              <p className="text-muted-foreground mb-4">
                {searchQuery ? 'No se encontraron facturas con los filtros aplicados' : 'Crea tu primera factura'}
              </p>
              {!searchQuery && (
                <Button onClick={() => navigate('/facturacion/nueva')}>
                  <Plus className="h-4 w-4 mr-2" />
                  Nueva Factura
                </Button>
              )}
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
}

