import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Plus, Search, User, Mail, Phone, MoreVertical, Edit, Trash2, Eye } from 'lucide-react';
import { Button } from '@shared/components/ui/button';
import { Input } from '@shared/components/ui/input';
import { Card, CardContent, CardHeader, CardTitle } from '@shared/components/ui/card';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@shared/components/ui/select';
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger } from '@shared/components/ui/dropdown-menu';
import { AlertDialog, AlertDialogAction, AlertDialogCancel, AlertDialogContent, AlertDialogDescription, AlertDialogFooter, AlertDialogHeader, AlertDialogTitle } from '@shared/components/ui/alert-dialog';
import { LoadingCards } from '@shared/components/common/LoadingCards';
import { Pagination } from '@shared/components/common/Pagination';
import { toast } from 'sonner';
import { propietarioService } from '@features/propietarios/services/propietarioService';
import { pacienteService } from '@features/pacientes/services/pacienteService';
import { Propietario, Paciente } from '@core/types';

export default function Propietarios() {
  const navigate = useNavigate();
  const [propietarios, setPropietarios] = useState<Propietario[]>([]);
  const [pacientes, setPacientes] = useState<Paciente[]>([]);
  const [searchTerm, setSearchTerm] = useState('');
  const [orderBy, setOrderBy] = useState<'nombre' | 'documento' | 'mascotas'>('nombre');
  const [isLoading, setIsLoading] = useState(true);
  const [deleteId, setDeleteId] = useState<string | null>(null);
  const [currentPage, setCurrentPage] = useState(1);
  const itemsPerPage = 9;

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      setIsLoading(true);
      const [propietariosData, pacientesData] = await Promise.all([
        propietarioService.getAll(),
        pacienteService.getAll(),
      ]);
      setPropietarios(propietariosData);
      setPacientes(pacientesData);
    } catch (error) {
      console.error('Error al cargar datos:', error);
      toast.error('Error al cargar los propietarios');
    } finally {
      setIsLoading(false);
    }
  };

  let filteredPropietarios = propietarios.filter(p =>
    p.nombre.toLowerCase().includes(searchTerm.toLowerCase()) ||
    p.email?.toLowerCase().includes(searchTerm.toLowerCase()) ||
    p.documento?.includes(searchTerm)
  );

  const getPacientesCount = (propietarioId: string) => 
    pacientes.filter(p => p.propietarioId === propietarioId).length;

  // Ordenar
  filteredPropietarios.sort((a, b) => {
    if (orderBy === 'nombre') return a.nombre.localeCompare(b.nombre);
    if (orderBy === 'documento') return (a.documento || '').localeCompare(b.documento || '');
    if (orderBy === 'mascotas') return getPacientesCount(b.id) - getPacientesCount(a.id);
    return 0;
  });

  // Paginación
  const totalPages = Math.ceil(filteredPropietarios.length / itemsPerPage);
  const paginatedPropietarios = filteredPropietarios.slice(
    (currentPage - 1) * itemsPerPage,
    currentPage * itemsPerPage
  );

  const handleDelete = async () => {
    if (!deleteId) return;

    try {
      await propietarioService.delete(deleteId);
      toast.success('Propietario eliminado exitosamente');
      setDeleteId(null);
      await loadData(); // Recargar los datos
    } catch (error: any) {
      console.error('Error al eliminar propietario:', error);
      toast.error(error.response?.data?.message || 'Error al eliminar el propietario');
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-3xl font-bold text-foreground">Propietarios</h1>
          <p className="text-muted-foreground mt-1">Gestión de propietarios y tutores</p>
        </div>
        <Button onClick={() => navigate('/propietarios/nuevo')} className="gap-2">
          <Plus className="h-4 w-4" />
          Nuevo Propietario
        </Button>
      </div>

      <div className="grid gap-4 md:grid-cols-3">
        <div className="relative md:col-span-2">
          <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-muted-foreground" />
          <Input
            placeholder="Buscar por nombre, documento o email..."
            className="pl-10"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
        </div>
        <Select value={orderBy} onValueChange={(v) => setOrderBy(v as any)}>
          <SelectTrigger>
            <SelectValue placeholder="Ordenar por" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="nombre">Nombre</SelectItem>
            <SelectItem value="documento">Documento</SelectItem>
            <SelectItem value="mascotas">Cantidad de Mascotas</SelectItem>
          </SelectContent>
        </Select>
      </div>

      {isLoading ? (
        <LoadingCards count={9} />
      ) : (
        <>
          <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
            {paginatedPropietarios.map((propietario) => (
              <Card key={propietario.id} className="hover:shadow-lg transition-shadow">
                <CardHeader className="pb-3">
                  <div className="flex items-start justify-between">
                    <div 
                      className="flex items-center gap-3 flex-1 cursor-pointer"
                      onClick={() => navigate(`/propietarios/${propietario.id}`)}
                    >
                      <div className="h-12 w-12 rounded-full bg-primary/10 flex items-center justify-center">
                        <User className="h-6 w-6 text-primary" />
                      </div>
                      <div className="flex-1">
                        <CardTitle className="text-lg">{propietario.nombre}</CardTitle>
                        <p className="text-sm text-muted-foreground">
                          {getPacientesCount(propietario.id)} {getPacientesCount(propietario.id) === 1 ? 'mascota' : 'mascotas'}
                        </p>
                      </div>
                    </div>
                    <DropdownMenu>
                      <DropdownMenuTrigger asChild onClick={(e) => e.stopPropagation()}>
                        <Button variant="ghost" size="icon" className="h-8 w-8">
                          <MoreVertical className="h-4 w-4" />
                        </Button>
                      </DropdownMenuTrigger>
                      <DropdownMenuContent align="end">
                        <DropdownMenuItem onClick={() => navigate(`/propietarios/${propietario.id}`)}>
                          <Eye className="h-4 w-4 mr-2" />
                          Ver Detalle
                        </DropdownMenuItem>
                        <DropdownMenuItem onClick={() => navigate(`/propietarios/${propietario.id}/editar`)}>
                          <Edit className="h-4 w-4 mr-2" />
                          Editar
                        </DropdownMenuItem>
                        <DropdownMenuItem 
                          onClick={() => setDeleteId(propietario.id)}
                          className="text-destructive"
                        >
                          <Trash2 className="h-4 w-4 mr-2" />
                          Eliminar
                        </DropdownMenuItem>
                      </DropdownMenuContent>
                    </DropdownMenu>
                  </div>
                </CardHeader>
                <CardContent className="space-y-2">
                  {propietario.documento && (
                    <div className="flex justify-between text-sm">
                      <span className="text-muted-foreground">Documento:</span>
                      <span className="font-medium">{propietario.documento}</span>
                    </div>
                  )}
                  {propietario.email && (
                    <div className="flex items-center gap-2 text-sm">
                      <Mail className="h-4 w-4 text-muted-foreground" />
                      <span className="truncate">{propietario.email}</span>
                    </div>
                  )}
                  {propietario.telefono && (
                    <div className="flex items-center gap-2 text-sm">
                      <Phone className="h-4 w-4 text-muted-foreground" />
                      <span>{propietario.telefono}</span>
                    </div>
                  )}
                </CardContent>
              </Card>
            ))}
          </div>

          {totalPages > 1 && (
            <Pagination
              currentPage={currentPage}
              totalPages={totalPages}
              onPageChange={setCurrentPage}
              itemsPerPage={itemsPerPage}
              totalItems={filteredPropietarios.length}
            />
          )}
        </>
      )}

      {!isLoading && filteredPropietarios.length === 0 && (
        <div className="text-center py-12">
          <User className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
          <h3 className="text-lg font-medium text-foreground">No se encontraron propietarios</h3>
          <p className="text-muted-foreground mt-1">
            {searchTerm ? 'Intenta con otros términos de búsqueda' : 'Comienza agregando un nuevo propietario'}
          </p>
        </div>
      )}

      <AlertDialog open={!!deleteId} onOpenChange={() => setDeleteId(null)}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>¿Eliminar propietario?</AlertDialogTitle>
            <AlertDialogDescription>
              Esta acción no se puede deshacer. El propietario será eliminado permanentemente del sistema.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>Cancelar</AlertDialogCancel>
            <AlertDialogAction onClick={handleDelete} className="bg-destructive text-destructive-foreground hover:bg-destructive/90">
              Eliminar
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  );
}
