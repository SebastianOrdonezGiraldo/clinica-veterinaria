import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Plus, Search, Edit, Trash2, Package, AlertTriangle, TrendingUp } from 'lucide-react';
import { Button } from '@shared/components/ui/button';
import { Input } from '@shared/components/ui/input';
import { Card, CardContent, CardHeader, CardTitle } from '@shared/components/ui/card';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@shared/components/ui/select';
import { Badge } from '@shared/components/ui/badge';
import { AlertDialog, AlertDialogAction, AlertDialogCancel, AlertDialogContent, AlertDialogDescription, AlertDialogFooter, AlertDialogHeader, AlertDialogTitle } from '@shared/components/ui/alert-dialog';
import { LoadingCards } from '@shared/components/common/LoadingCards';
import { useProductos, useDeleteProducto, useCategorias } from '../hooks/useInventario';
import { useDebounce } from '@shared/hooks/useDebounce';
import { Producto } from '../services/inventarioService';

export default function Productos() {
  const navigate = useNavigate();
  const [searchTerm, setSearchTerm] = useState('');
  const [categoriaFiltro, setCategoriaFiltro] = useState<string>('todas');
  const [deleteId, setDeleteId] = useState<number | null>(null);
  const debouncedSearch = useDebounce(searchTerm, 300);

  const { data: productos = [], isLoading } = useProductos();
  const { data: categorias = [] } = useCategorias();
  const { mutate: deleteProducto, isPending: isDeleting } = useDeleteProducto();

  const filteredProductos = productos.filter(prod => {
    const matchSearch = 
      prod.nombre.toLowerCase().includes(debouncedSearch.toLowerCase()) ||
      prod.codigo.toLowerCase().includes(debouncedSearch.toLowerCase());
    const matchCategoria = categoriaFiltro === 'todas' || prod.categoriaId.toString() === categoriaFiltro;
    return matchSearch && matchCategoria;
  });

  const handleDelete = () => {
    if (deleteId) {
      deleteProducto(deleteId);
      setDeleteId(null);
    }
  };

  const tieneStockBajo = (producto: Producto) => {
    return producto.stockMinimo !== undefined && producto.stockActual <= producto.stockMinimo;
  };

  const tieneSobrestock = (producto: Producto) => {
    return producto.stockMaximo !== undefined && producto.stockActual > producto.stockMaximo;
  };

  if (isLoading) {
    return <LoadingCards count={6} />;
  }

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-3xl font-bold text-foreground">Productos</h1>
          <p className="text-muted-foreground mt-1">Gestión de productos del inventario</p>
        </div>
        <Button onClick={() => navigate('/inventario/productos/nuevo')} className="gap-2">
          <Plus className="h-4 w-4" />
          Nuevo Producto
        </Button>
      </div>

      <div className="grid gap-4 md:grid-cols-4">
        <div className="relative md:col-span-2">
          <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-muted-foreground" />
          <Input
            placeholder="Buscar por nombre o código..."
            className="pl-10"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
        </div>
        <Select value={categoriaFiltro} onValueChange={setCategoriaFiltro}>
          <SelectTrigger>
            <SelectValue placeholder="Filtrar por categoría" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="todas">Todas las categorías</SelectItem>
            {categorias.map((cat) => (
              <SelectItem key={cat.id} value={cat.id.toString()}>
                {cat.nombre}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>
        <Button
          variant="outline"
          onClick={() => navigate('/inventario/movimientos')}
          className="gap-2"
        >
          <TrendingUp className="h-4 w-4" />
          Movimientos
        </Button>
      </div>

      {filteredProductos.length === 0 ? (
        <Card>
          <CardContent className="flex flex-col items-center justify-center py-12">
            <Package className="h-12 w-12 text-muted-foreground mb-4" />
            <p className="text-muted-foreground text-center">
              {searchTerm || categoriaFiltro !== 'todas' 
                ? 'No se encontraron productos' 
                : 'No hay productos registrados'}
            </p>
          </CardContent>
        </Card>
      ) : (
        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
          {filteredProductos.map((producto) => (
            <Card 
              key={producto.id} 
              className="hover:shadow-md transition-shadow"
            >
              <CardHeader>
                <div className="flex justify-between items-start">
                  <div className="flex-1">
                    <CardTitle className="text-lg">{producto.nombre}</CardTitle>
                    <p className="text-sm text-muted-foreground mt-1">
                      Código: {producto.codigo}
                    </p>
                  </div>
                  <div className="flex gap-2">
                    <Button
                      variant="ghost"
                      size="icon"
                      onClick={() => navigate(`/inventario/productos/${producto.id}/editar`)}
                    >
                      <Edit className="h-4 w-4" />
                    </Button>
                    <Button
                      variant="ghost"
                      size="icon"
                      onClick={() => setDeleteId(producto.id)}
                      disabled={isDeleting}
                    >
                      <Trash2 className="h-4 w-4 text-destructive" />
                    </Button>
                  </div>
                </div>
              </CardHeader>
              <CardContent className="space-y-3">
                <div className="flex items-center justify-between">
                  <span className="text-sm text-muted-foreground">Categoría:</span>
                  <Badge variant="secondary">{producto.categoriaNombre || 'Sin categoría'}</Badge>
                </div>
                
                <div className="flex items-center justify-between">
                  <span className="text-sm text-muted-foreground">Stock:</span>
                  <div className="flex items-center gap-2">
                    <span className={`font-semibold ${
                      tieneStockBajo(producto) 
                        ? 'text-red-600 dark:text-red-400' 
                        : tieneSobrestock(producto)
                        ? 'text-orange-600 dark:text-orange-400'
                        : 'text-green-600 dark:text-green-400'
                    }`}>
                      {producto.stockActual} {producto.unidadMedida}
                    </span>
                    {tieneStockBajo(producto) && (
                      <AlertTriangle className="h-4 w-4 text-red-600 dark:text-red-400" />
                    )}
                  </div>
                </div>

                {producto.stockMinimo !== undefined && (
                  <div className="flex items-center justify-between text-sm">
                    <span className="text-muted-foreground">Mínimo:</span>
                    <span>{producto.stockMinimo} {producto.unidadMedida}</span>
                  </div>
                )}

                <div className="flex items-center justify-between text-sm">
                  <span className="text-muted-foreground">Costo:</span>
                  <span className="font-medium">${producto.costo.toFixed(2)}</span>
                </div>

                {producto.precioVenta && (
                  <div className="flex items-center justify-between text-sm">
                    <span className="text-muted-foreground">Precio Venta:</span>
                    <span className="font-medium">${producto.precioVenta.toFixed(2)}</span>
                  </div>
                )}

                <div className="mt-4 flex items-center gap-2">
                  <span className={`text-xs px-2 py-1 rounded-full ${
                    producto.activo
                      ? 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200'
                      : 'bg-gray-100 text-gray-800 dark:bg-gray-800 dark:text-gray-200'
                  }`}>
                    {producto.activo ? 'Activo' : 'Inactivo'}
                  </span>
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      )}

      <AlertDialog open={!!deleteId} onOpenChange={(open) => !open && setDeleteId(null)}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>¿Desactivar producto?</AlertDialogTitle>
            <AlertDialogDescription>
              Esta acción desactivará el producto. No se podrá usar en nuevos movimientos,
              pero el historial se mantendrá.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>Cancelar</AlertDialogCancel>
            <AlertDialogAction onClick={handleDelete} disabled={isDeleting}>
              {isDeleting ? 'Desactivando...' : 'Desactivar'}
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  );
}

