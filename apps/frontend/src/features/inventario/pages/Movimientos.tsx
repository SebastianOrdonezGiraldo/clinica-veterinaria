import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Plus, Search, ArrowDown, ArrowUp, RefreshCw, Package } from 'lucide-react';
import { Button } from '@shared/components/ui/button';
import { Input } from '@shared/components/ui/input';
import { Card, CardContent, CardHeader, CardTitle } from '@shared/components/ui/card';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@shared/components/ui/select';
import { Badge } from '@shared/components/ui/badge';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@shared/components/ui/table';
import { LoadingCards } from '@shared/components/common/LoadingCards';
import { useMovimientosByTipo, useProductos } from '../hooks/useInventario';
import { useDebounce } from '@shared/hooks/useDebounce';
import { TipoMovimiento, MovimientoInventario } from '../services/inventarioService';
import { format } from 'date-fns';

export default function Movimientos() {
  const navigate = useNavigate();
  const [tipoFiltro, setTipoFiltro] = useState<TipoMovimiento | 'todos'>('todos');
  const [searchTerm, setSearchTerm] = useState('');
  const debouncedSearch = useDebounce(searchTerm, 300);

  const { data: movimientosEntrada = [], isLoading: isLoadingEntrada } = useMovimientosByTipo(
    tipoFiltro === 'todos' || tipoFiltro === 'ENTRADA' ? 'ENTRADA' : null
  );
  const { data: movimientosSalida = [], isLoading: isLoadingSalida } = useMovimientosByTipo(
    tipoFiltro === 'todos' || tipoFiltro === 'SALIDA' ? 'SALIDA' : null
  );
  const { data: movimientosAjuste = [], isLoading: isLoadingAjuste } = useMovimientosByTipo(
    tipoFiltro === 'todos' || tipoFiltro === 'AJUSTE' ? 'AJUSTE' : null
  );

  const allMovimientos = [
    ...movimientosEntrada,
    ...movimientosSalida,
    ...movimientosAjuste,
  ].sort((a, b) => new Date(b.fecha).getTime() - new Date(a.fecha).getTime());

  const filteredMovimientos = allMovimientos.filter(mov =>
    mov.productoNombre?.toLowerCase().includes(debouncedSearch.toLowerCase()) ||
    mov.productoCodigo?.toLowerCase().includes(debouncedSearch.toLowerCase()) ||
    mov.motivo?.toLowerCase().includes(debouncedSearch.toLowerCase())
  );

  const isLoading = isLoadingEntrada || isLoadingSalida || isLoadingAjuste;

  const getTipoBadge = (tipo: TipoMovimiento) => {
    switch (tipo) {
      case 'ENTRADA':
        return <Badge className="bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200">Entrada</Badge>;
      case 'SALIDA':
        return <Badge className="bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200">Salida</Badge>;
      case 'AJUSTE':
        return <Badge className="bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-200">Ajuste</Badge>;
    }
  };

  const getTipoIcon = (tipo: TipoMovimiento) => {
    switch (tipo) {
      case 'ENTRADA':
        return <ArrowDown className="h-4 w-4 text-green-600" />;
      case 'SALIDA':
        return <ArrowUp className="h-4 w-4 text-red-600" />;
      case 'AJUSTE':
        return <RefreshCw className="h-4 w-4 text-blue-600" />;
    }
  };

  if (isLoading) {
    return <LoadingCards count={5} />;
  }

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-3xl font-bold text-foreground">Movimientos de Inventario</h1>
          <p className="text-muted-foreground mt-1">Historial de entradas, salidas y ajustes</p>
        </div>
        <div className="flex gap-2">
          <Button
            variant="outline"
            onClick={() => navigate('/inventario/movimientos/entrada')}
            className="gap-2"
          >
            <ArrowDown className="h-4 w-4" />
            Entrada
          </Button>
          <Button
            variant="outline"
            onClick={() => navigate('/inventario/movimientos/salida')}
            className="gap-2"
          >
            <ArrowUp className="h-4 w-4" />
            Salida
          </Button>
          <Button
            variant="outline"
            onClick={() => navigate('/inventario/movimientos/ajuste')}
            className="gap-2"
          >
            <RefreshCw className="h-4 w-4" />
            Ajuste
          </Button>
        </div>
      </div>

      <div className="grid gap-4 md:grid-cols-4">
        <div className="relative md:col-span-2">
          <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-muted-foreground" />
          <Input
            placeholder="Buscar por producto o motivo..."
            className="pl-10"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
        </div>
        <Select value={tipoFiltro} onValueChange={(value) => setTipoFiltro(value as TipoMovimiento | 'todos')}>
          <SelectTrigger>
            <SelectValue placeholder="Filtrar por tipo" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="todos">Todos los tipos</SelectItem>
            <SelectItem value="ENTRADA">Entradas</SelectItem>
            <SelectItem value="SALIDA">Salidas</SelectItem>
            <SelectItem value="AJUSTE">Ajustes</SelectItem>
          </SelectContent>
        </Select>
        <Button
          variant="outline"
          onClick={() => navigate('/inventario/productos')}
          className="gap-2"
        >
          <Package className="h-4 w-4" />
          Productos
        </Button>
      </div>

      {filteredMovimientos.length === 0 ? (
        <Card>
          <CardContent className="flex flex-col items-center justify-center py-12">
            <Package className="h-12 w-12 text-muted-foreground mb-4" />
            <p className="text-muted-foreground text-center">
              {searchTerm || tipoFiltro !== 'todos'
                ? 'No se encontraron movimientos'
                : 'No hay movimientos registrados'}
            </p>
          </CardContent>
        </Card>
      ) : (
        <Card>
          <CardHeader>
            <CardTitle>Historial de Movimientos</CardTitle>
          </CardHeader>
          <CardContent>
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Fecha</TableHead>
                  <TableHead>Tipo</TableHead>
                  <TableHead>Producto</TableHead>
                  <TableHead>Cantidad</TableHead>
                  <TableHead>Stock Anterior</TableHead>
                  <TableHead>Stock Resultante</TableHead>
                  <TableHead>Motivo</TableHead>
                  <TableHead>Usuario</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {filteredMovimientos.map((movimiento) => (
                  <TableRow key={movimiento.id}>
                    <TableCell>
                      {format(new Date(movimiento.fecha), "dd/MM/yyyy HH:mm")}
                    </TableCell>
                    <TableCell>
                      <div className="flex items-center gap-2">
                        {getTipoIcon(movimiento.tipo)}
                        {getTipoBadge(movimiento.tipo)}
                      </div>
                    </TableCell>
                    <TableCell>
                      <div>
                        <div className="font-medium">{movimiento.productoNombre}</div>
                        <div className="text-sm text-muted-foreground">{movimiento.productoCodigo}</div>
                      </div>
                    </TableCell>
                    <TableCell className="font-medium">
                      {movimiento.cantidad}
                    </TableCell>
                    <TableCell>
                      {movimiento.stockAnterior !== undefined ? movimiento.stockAnterior : '-'}
                    </TableCell>
                    <TableCell className="font-semibold">
                      {movimiento.stockResultante !== undefined ? movimiento.stockResultante : '-'}
                    </TableCell>
                    <TableCell className="max-w-xs truncate" title={movimiento.motivo}>
                      {movimiento.motivo}
                    </TableCell>
                    <TableCell>
                      {movimiento.usuarioNombre || '-'}
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </CardContent>
        </Card>
      )}
    </div>
  );
}

