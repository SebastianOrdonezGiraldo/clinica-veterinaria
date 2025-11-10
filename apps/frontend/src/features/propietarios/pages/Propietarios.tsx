import { useState, useEffect, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import { Plus, Search, User, Mail, Phone, MoreVertical, Edit, Trash2, Eye, Inbox } from 'lucide-react';
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
  const [debouncedSearchTerm, setDebouncedSearchTerm] = useState('');
  const [orderBy, setOrderBy] = useState<'nombre' | 'documento' | 'mascotas'>('nombre');
  const [isLoading, setIsLoading] = useState(true);
  const [deleteId, setDeleteId] = useState<string | null>(null);
  const [isDeleting, setIsDeleting] = useState(false);
  const [currentPage, setCurrentPage] = useState(1);
  const itemsPerPage = 9;

  useEffect(() => {
    loadData();
  }, []);

  // Debounce para la búsqueda
  useEffect(() => {
    const timer = setTimeout(() => {
      setDebouncedSearchTerm(searchTerm);
      setCurrentPage(1); // Resetear a la primera página al buscar
    }, 300);

    return () => clearTimeout(timer);
  }, [searchTerm]);

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

  // Filtrar con el término de búsqueda con debounce
  const filteredPropietarios = useMemo(() => {
    return propietarios.filter(p =>
      p.nombre.toLowerCase().includes(debouncedSearchTerm.toLowerCase()) ||
      p.email?.toLowerCase().includes(debouncedSearchTerm.toLowerCase()) ||
      p.documento?.includes(debouncedSearchTerm)
    );
  }, [propietarios, debouncedSearchTerm]);

  const getPacientesCount = (propietarioId: string) => 
    pacientes.filter(p => p.propietarioId === propietarioId).length;

  // Ordenar y paginar
  const sortedAndPaginatedPropietarios = useMemo(() => {
    const sorted = [...filteredPropietarios].sort((a, b) => {
      if (orderBy === 'nombre') return a.nombre.localeCompare(b.nombre);
      if (orderBy === 'documento') return (a.documento || '').localeCompare(b.documento || '');
      if (orderBy === 'mascotas') return getPacientesCount(b.id) - getPacientesCount(a.id);
      return 0;
    });

    return sorted.slice(
      (currentPage - 1) * itemsPerPage,
      currentPage * itemsPerPage
    );
  }, [filteredPropietarios, orderBy, currentPage, pacientes]);

  const totalPages = Math.ceil(filteredPropietarios.length / itemsPerPage);

  const handleDelete = async () => {
    if (!deleteId) return;

    try {
      setIsDeleting(true);
      // Optimistic update: remover de la lista inmediatamente
      setPropietarios(prev => prev.filter(p => p.id !== deleteId));
      setDeleteId(null);

      await propietarioService.delete(deleteId);
      toast.success('Propietario eliminado exitosamente');
    } catch (error: any) {
      console.error('Error al eliminar propietario:', error);
      // Revertir el cambio optimista en caso de error
      await loadData();
      toast.error(error.response?.data?.message || 'Error al eliminar el propietario');
    } finally {
      setIsDeleting(false);
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
            {sortedAndPaginatedPropietarios.map((propietario) => (
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
        <Card className="border-dashed">
          <CardContent className="flex flex-col items-center justify-center py-16">
            <div className="rounded-full bg-muted p-4 mb-4">
              {searchTerm ? (
                <Search className="h-8 w-8 text-muted-foreground" />
              ) : (
                <Inbox className="h-8 w-8 text-muted-foreground" />
              )}
            </div>
            <h3 className="text-lg font-semibold text-foreground mb-2">
              {searchTerm ? 'No se encontraron resultados' : 'No hay propietarios registrados'}
            </h3>
            <p className="text-sm text-muted-foreground text-center max-w-sm mb-4">
              {searchTerm 
                ? `No se encontraron propietarios que coincidan con "${searchTerm}". Intenta con otros términos de búsqueda.`
                : 'Comienza agregando tu primer propietario para gestionar la información de los dueños de las mascotas.'}
            </p>
            {!searchTerm && (
              <Button onClick={() => navigate('/propietarios/nuevo')} className="gap-2">
                <Plus className="h-4 w-4" />
                Agregar Primer Propietario
              </Button>
            )}
          </CardContent>
        </Card>
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
            <AlertDialogAction 
              onClick={handleDelete} 
              className="bg-destructive text-destructive-foreground hover:bg-destructive/90"
              disabled={isDeleting}
            >
              {isDeleting ? 'Eliminando...' : 'Eliminar'}
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  );
}
