import { useState, useEffect, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import { Plus, Search } from 'lucide-react';
import { Button } from '@shared/components/ui/button';
import { Input } from '@shared/components/ui/input';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@shared/components/ui/select';
import { AlertDialog, AlertDialogAction, AlertDialogCancel, AlertDialogContent, AlertDialogDescription, AlertDialogFooter, AlertDialogHeader, AlertDialogTitle } from '@shared/components/ui/alert-dialog';
import { LoadingCards } from '@shared/components/common/LoadingCards';
import { Pagination } from '@shared/components/common/Pagination';
import { useDebounce } from '@shared/hooks/useDebounce';
import { usePacientes } from '../hooks/usePacientes';
import { useAllPropietarios } from '@features/propietarios/hooks/usePropietarios';
import { PacienteCard } from '../components/PacienteCard';

export default function Pacientes() {
  const navigate = useNavigate();
  const [searchTerm, setSearchTerm] = useState('');
  const [especieFiltro, setEspecieFiltro] = useState('todos');
  const [orderBy, setOrderBy] = useState<'nombre' | 'especie' | 'edad'>('nombre');
  const [deleteId, setDeleteId] = useState<string | null>(null);
  const [currentPage, setCurrentPage] = useState(0);
  const itemsPerPage = 9;

  // Debounce del término de búsqueda
  const debouncedSearchTerm = useDebounce(searchTerm, 500);

  // Cargar propietarios con React Query usando hook personalizado
  const { propietarios = [] } = useAllPropietarios();

  // Mapear orderBy a formato Spring
  const sortParam = useMemo(() => {
    if (orderBy === 'especie') return 'especie,asc';
    if (orderBy === 'edad') return 'edadMeses,desc';
    return 'nombre,asc';
  }, [orderBy]);

  // Usar hook personalizado con React Query
  const {
    pacientesPage,
    pacientes,
    isLoading,
    deletePaciente,
    isDeleting,
  } = usePacientes({
    nombre: debouncedSearchTerm || undefined,
    especie: especieFiltro !== 'todos' ? especieFiltro : undefined,
    page: currentPage,
    size: itemsPerPage,
    sort: sortParam,
  });

  // Resetear página cuando cambia la búsqueda
  useEffect(() => {
    setCurrentPage(0);
  }, [debouncedSearchTerm]);

  const getPropietario = (id: string) => propietarios.find(p => p.id === id);
  
  const totalPages = pacientesPage?.totalPages || 0;
  const totalElements = pacientesPage?.totalElements || 0;

  const handleDelete = async () => {
    if (!deleteId) return;
    deletePaciente(deleteId);
    setDeleteId(null);
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
          <div 
            className="grid gap-4 md:grid-cols-2 lg:grid-cols-3"
            role="list"
            aria-label="Lista de pacientes"
          >
            {pacientes.map((paciente) => {
              const propietario = getPropietario(paciente.propietarioId);
              return (
                <PacienteCard
                  key={paciente.id}
                  paciente={paciente}
                  propietario={propietario}
                  onDelete={setDeleteId}
                />
              );
            })}
          </div>

          {totalPages > 1 && (
            <Pagination
              currentPage={currentPage + 1} // Mostrar 1-indexed al usuario
              totalPages={totalPages}
              onPageChange={(page) => setCurrentPage(page - 1)} // Convertir a 0-indexed para backend
              itemsPerPage={itemsPerPage}
              totalItems={totalElements}
            />
          )}
        </>
      )}

      {!isLoading && pacientes.length === 0 && (
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
