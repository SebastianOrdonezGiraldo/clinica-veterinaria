import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Plus, Search, Edit, Trash2, Building2, Mail, Phone } from 'lucide-react';
import { Button } from '@shared/components/ui/button';
import { Input } from '@shared/components/ui/input';
import { Card, CardContent, CardHeader, CardTitle } from '@shared/components/ui/card';
import { AlertDialog, AlertDialogAction, AlertDialogCancel, AlertDialogContent, AlertDialogDescription, AlertDialogFooter, AlertDialogHeader, AlertDialogTitle } from '@shared/components/ui/alert-dialog';
import { LoadingCards } from '@shared/components/common/LoadingCards';
import { useProveedores, useDeleteProveedor } from '../hooks/useInventario';
import { useDebounce } from '@shared/hooks/useDebounce';

export default function Proveedores() {
  const navigate = useNavigate();
  const [searchTerm, setSearchTerm] = useState('');
  const [deleteId, setDeleteId] = useState<number | null>(null);
  const debouncedSearch = useDebounce(searchTerm, 300);

  const { data: proveedores = [], isLoading } = useProveedores();
  const { mutate: deleteProveedor, isPending: isDeleting } = useDeleteProveedor();

  const filteredProveedores = proveedores.filter(prov =>
    prov.nombre.toLowerCase().includes(debouncedSearch.toLowerCase()) ||
    prov.email?.toLowerCase().includes(debouncedSearch.toLowerCase()) ||
    prov.ruc?.toLowerCase().includes(debouncedSearch.toLowerCase())
  );

  const handleDelete = () => {
    if (deleteId) {
      deleteProveedor(deleteId);
      setDeleteId(null);
    }
  };

  if (isLoading) {
    return <LoadingCards count={6} />;
  }

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-3xl font-bold text-foreground">Proveedores</h1>
          <p className="text-muted-foreground mt-1">Gestión de proveedores de productos</p>
        </div>
        <Button onClick={() => navigate('/inventario/proveedores/nuevo')} className="gap-2">
          <Plus className="h-4 w-4" />
          Nuevo Proveedor
        </Button>
      </div>

      <div className="relative">
        <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-muted-foreground" />
        <Input
          placeholder="Buscar por nombre, email o RUC..."
          className="pl-10"
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
        />
      </div>

      {filteredProveedores.length === 0 ? (
        <Card>
          <CardContent className="flex flex-col items-center justify-center py-12">
            <Building2 className="h-12 w-12 text-muted-foreground mb-4" />
            <p className="text-muted-foreground text-center">
              {searchTerm ? 'No se encontraron proveedores' : 'No hay proveedores registrados'}
            </p>
          </CardContent>
        </Card>
      ) : (
        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
          {filteredProveedores.map((proveedor) => (
            <Card key={proveedor.id} className="hover:shadow-md transition-shadow">
              <CardHeader>
                <div className="flex justify-between items-start">
                  <CardTitle className="text-lg">{proveedor.nombre}</CardTitle>
                  <div className="flex gap-2">
                    <Button
                      variant="ghost"
                      size="icon"
                      onClick={() => navigate(`/inventario/proveedores/${proveedor.id}/editar`)}
                    >
                      <Edit className="h-4 w-4" />
                    </Button>
                    <Button
                      variant="ghost"
                      size="icon"
                      onClick={() => setDeleteId(proveedor.id)}
                      disabled={isDeleting}
                    >
                      <Trash2 className="h-4 w-4 text-destructive" />
                    </Button>
                  </div>
                </div>
              </CardHeader>
              <CardContent className="space-y-2">
                {proveedor.ruc && (
                  <div className="flex items-center gap-2 text-sm text-muted-foreground">
                    <span className="font-medium">RUC:</span>
                    <span>{proveedor.ruc}</span>
                  </div>
                )}
                {proveedor.email && (
                  <div className="flex items-center gap-2 text-sm text-muted-foreground">
                    <Mail className="h-4 w-4" />
                    <span>{proveedor.email}</span>
                  </div>
                )}
                {proveedor.telefono && (
                  <div className="flex items-center gap-2 text-sm text-muted-foreground">
                    <Phone className="h-4 w-4" />
                    <span>{proveedor.telefono}</span>
                  </div>
                )}
                <div className="mt-4 flex items-center gap-2">
                  <span className={`text-xs px-2 py-1 rounded-full ${
                    proveedor.activo
                      ? 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200'
                      : 'bg-gray-100 text-gray-800 dark:bg-gray-800 dark:text-gray-200'
                  }`}>
                    {proveedor.activo ? 'Activo' : 'Inactivo'}
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
            <AlertDialogTitle>¿Desactivar proveedor?</AlertDialogTitle>
            <AlertDialogDescription>
              Esta acción desactivará el proveedor. No se podrá usar en nuevos movimientos,
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

