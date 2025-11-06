import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Plus, Search, Dog, MoreVertical, Edit, Trash2, Eye } from 'lucide-react';
import { Button } from '@shared/components/ui/button';
import { Input } from '@shared/components/ui/input';
import { Card, CardContent, CardHeader, CardTitle } from '@shared/components/ui/card';
import { Badge } from '@shared/components/ui/badge';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@shared/components/ui/select';
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger } from '@shared/components/ui/dropdown-menu';
import { AlertDialog, AlertDialogAction, AlertDialogCancel, AlertDialogContent, AlertDialogDescription, AlertDialogFooter, AlertDialogHeader, AlertDialogTitle } from '@shared/components/ui/alert-dialog';
import { LoadingCards } from '@shared/components/common/LoadingCards';
import { Pagination } from '@shared/components/common/Pagination';
import { toast } from 'sonner';
import { pacienteService } from '@features/pacientes/services/pacienteService';
import { propietarioService } from '@features/propietarios/services/propietarioService';
import { Paciente, Propietario } from '@core/types';

export default function Pacientes() {
  const navigate = useNavigate();
  const [pacientes, setPacientes] = useState<Paciente[]>([]);
  const [propietarios, setPropietarios] = useState<Propietario[]>([]);
  const [searchTerm, setSearchTerm] = useState('');
  const [especieFiltro, setEspecieFiltro] = useState('todos');
  const [orderBy, setOrderBy] = useState<'nombre' | 'especie' | 'edad'>('nombre');
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
      const [pacientesData, propietariosData] = await Promise.all([
        pacienteService.getAll(),
        propietarioService.getAll(),
      ]);
      setPacientes(pacientesData);
      setPropietarios(propietariosData);
    } catch (error) {
      console.error('Error al cargar datos:', error);
      toast.error('Error al cargar los pacientes');
    } finally {
      setIsLoading(false);
    }
  };

  let filteredPacientes = pacientes.filter(p => {
    const matchSearch = p.nombre.toLowerCase().includes(searchTerm.toLowerCase()) ||
      p.especie.toLowerCase().includes(searchTerm.toLowerCase()) ||
      p.raza?.toLowerCase().includes(searchTerm.toLowerCase());
    const matchEspecie = especieFiltro === 'todos' || p.especie === especieFiltro;
    return matchSearch && matchEspecie;
  });

  // Ordenar
  filteredPacientes.sort((a, b) => {
    if (orderBy === 'nombre') return a.nombre.localeCompare(b.nombre);
    if (orderBy === 'especie') return a.especie.localeCompare(b.especie);
    if (orderBy === 'edad') return (b.edadMeses || 0) - (a.edadMeses || 0);
    return 0;
  });

  // Paginación
  const totalPages = Math.ceil(filteredPacientes.length / itemsPerPage);
  const paginatedPacientes = filteredPacientes.slice(
    (currentPage - 1) * itemsPerPage,
    currentPage * itemsPerPage
  );

  const getPropietario = (id: string) => propietarios.find(p => p.id === id);

  const handleDelete = async () => {
    if (!deleteId) return;

    try {
      await pacienteService.delete(deleteId);
      toast.success('Paciente eliminado exitosamente');
      setDeleteId(null);
      await loadData(); // Recargar los datos
    } catch (error: any) {
      console.error('Error al eliminar paciente:', error);
      toast.error(error.response?.data?.message || 'Error al eliminar el paciente');
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-3xl font-bold text-foreground">Pacientes</h1>
          <p className="text-muted-foreground mt-1">Gestión de pacientes de la clínica</p>
        </div>
        <Button onClick={() => navigate('/pacientes/nuevo')} className="gap-2">
          <Plus className="h-4 w-4" />
          Nuevo Paciente
        </Button>
      </div>

      <div className="grid gap-4 md:grid-cols-4">
        <div className="relative md:col-span-2">
          <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-muted-foreground" />
          <Input
            placeholder="Buscar por nombre, especie o raza..."
            className="pl-10"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
        </div>
        <Select value={especieFiltro} onValueChange={setEspecieFiltro}>
          <SelectTrigger>
            <SelectValue placeholder="Filtrar por especie" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="todos">Todas las especies</SelectItem>
            <SelectItem value="Canino">Canino</SelectItem>
            <SelectItem value="Felino">Felino</SelectItem>
            <SelectItem value="Otro">Otro</SelectItem>
          </SelectContent>
        </Select>
        <Select value={orderBy} onValueChange={(v) => setOrderBy(v as any)}>
          <SelectTrigger>
            <SelectValue placeholder="Ordenar por" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="nombre">Nombre</SelectItem>
            <SelectItem value="especie">Especie</SelectItem>
            <SelectItem value="edad">Edad</SelectItem>
          </SelectContent>
        </Select>
      </div>

      {isLoading ? (
        <LoadingCards count={9} />
      ) : (
        <>
          <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
            {paginatedPacientes.map((paciente) => {
          const propietario = getPropietario(paciente.propietarioId);
          return (
            <Card key={paciente.id} className="hover:shadow-lg transition-shadow">
              <CardHeader className="pb-3">
                <div className="flex items-start justify-between">
                  <div 
                    className="flex items-center gap-3 flex-1 cursor-pointer"
                    onClick={() => navigate(`/pacientes/${paciente.id}`)}
                  >
                    <div className="h-12 w-12 rounded-full bg-primary/10 flex items-center justify-center">
                      <Dog className="h-6 w-6 text-primary" />
                    </div>
                    <div>
                      <CardTitle className="text-lg">{paciente.nombre}</CardTitle>
                      <p className="text-sm text-muted-foreground">{paciente.raza || 'Sin raza'}</p>
                    </div>
                  </div>
                  <div className="flex items-center gap-2">
                    <Badge variant="outline" className="bg-secondary/10 text-secondary border-secondary/20">
                      {paciente.especie}
                    </Badge>
                    <DropdownMenu>
                      <DropdownMenuTrigger asChild onClick={(e) => e.stopPropagation()}>
                        <Button variant="ghost" size="icon" className="h-8 w-8">
                          <MoreVertical className="h-4 w-4" />
                        </Button>
                      </DropdownMenuTrigger>
                      <DropdownMenuContent align="end">
                        <DropdownMenuItem onClick={() => navigate(`/pacientes/${paciente.id}`)}>
                          <Eye className="h-4 w-4 mr-2" />
                          Ver Detalle
                        </DropdownMenuItem>
                        <DropdownMenuItem onClick={() => navigate(`/pacientes/${paciente.id}/editar`)}>
                          <Edit className="h-4 w-4 mr-2" />
                          Editar
                        </DropdownMenuItem>
                        <DropdownMenuItem 
                          onClick={() => setDeleteId(paciente.id)}
                          className="text-destructive"
                        >
                          <Trash2 className="h-4 w-4 mr-2" />
                          Eliminar
                        </DropdownMenuItem>
                      </DropdownMenuContent>
                    </DropdownMenu>
                  </div>
                </div>
              </CardHeader>
              <CardContent className="space-y-2">
                <div className="flex justify-between text-sm">
                  <span className="text-muted-foreground">Sexo:</span>
                  <span className="font-medium">{paciente.sexo === 'M' ? 'Macho' : 'Hembra'}</span>
                </div>
                <div className="flex justify-between text-sm">
                  <span className="text-muted-foreground">Edad:</span>
                  <span className="font-medium">{paciente.edadMeses ? `${Math.floor(paciente.edadMeses / 12)}a ${paciente.edadMeses % 12}m` : 'N/A'}</span>
                </div>
                <div className="flex justify-between text-sm">
                  <span className="text-muted-foreground">Peso:</span>
                  <span className="font-medium">{paciente.pesoKg ? `${paciente.pesoKg} kg` : 'N/A'}</span>
                </div>
                <div className="pt-2 border-t">
                  <p className="text-xs text-muted-foreground">Propietario</p>
                  <p className="text-sm font-medium">{propietario?.nombre}</p>
                </div>
              </CardContent>
            </Card>
          );
        })}
          </div>

          {totalPages > 1 && (
            <Pagination
              currentPage={currentPage}
              totalPages={totalPages}
              onPageChange={setCurrentPage}
              itemsPerPage={itemsPerPage}
              totalItems={filteredPacientes.length}
            />
          )}
        </>
      )}

      {!isLoading && filteredPacientes.length === 0 && (
        <div className="text-center py-12">
          <Dog className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
          <h3 className="text-lg font-medium text-foreground">No se encontraron pacientes</h3>
          <p className="text-muted-foreground mt-1">
            {searchTerm ? 'Intenta con otros términos de búsqueda' : 'Comienza agregando un nuevo paciente'}
          </p>
        </div>
      )}

      <AlertDialog open={!!deleteId} onOpenChange={() => setDeleteId(null)}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>¿Eliminar paciente?</AlertDialogTitle>
            <AlertDialogDescription>
              Esta acción no se puede deshacer. El paciente y su historial serán eliminados permanentemente.
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
