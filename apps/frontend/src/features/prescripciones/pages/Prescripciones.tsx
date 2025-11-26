import { useState, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import { Pill, FileText, Calendar, User, Plus, Search } from 'lucide-react';
import { Card, CardContent, CardHeader, CardTitle } from '@shared/components/ui/card';
import { Button } from '@shared/components/ui/button';
import { Badge } from '@shared/components/ui/badge';
import { Input } from '@shared/components/ui/input';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@shared/components/ui/select';
import { LoadingCards } from '@shared/components/common/LoadingCards';
import { Pagination } from '@shared/components/common/Pagination';
import { EmptyState } from '@shared/components/common/EmptyState';
import { ErrorState } from '@shared/components/common/ErrorState';
import { useDebounce } from '@shared/hooks/useDebounce';
import { usePrescripciones } from '../hooks/usePrescripciones';
import { useAllPacientes } from '@features/pacientes/hooks/usePacientes';
import { useQuery } from '@tanstack/react-query';
import { consultaService } from '@features/historias/services/consultaService';
import { usuarioService } from '@features/usuarios/services/usuarioService';
import { Consulta, Usuario } from '@core/types';

export default function Prescripciones() {
  const navigate = useNavigate();
  const [searchTerm, setSearchTerm] = useState('');
  const [filtroPaciente, setFiltroPaciente] = useState<string>('todos');
  const [currentPage, setCurrentPage] = useState(0);
  const itemsPerPage = 10;

  const debouncedSearchTerm = useDebounce(searchTerm, 500);

  // Cargar pacientes para el filtro
  const { pacientes = [] } = useAllPacientes();

  // Cargar consultas y usuarios con React Query (cache)
  const { data: consultas = [] } = useQuery<Consulta[]>({
    queryKey: ['consultas', 'all'],
    queryFn: () => consultaService.getAll(),
    staleTime: 60000,
  });

  const { data: usuarios = [] } = useQuery<Usuario[]>({
    queryKey: ['usuarios', 'all'],
    queryFn: () => usuarioService.getAll(),
    staleTime: 60000,
  });

  // Usar hook personalizado con React Query y paginación
  const {
    prescripcionesPage,
    prescripciones,
    isLoading,
    error,
    refetch,
  } = usePrescripciones({
    pacienteId: filtroPaciente !== 'todos' ? filtroPaciente : undefined,
    page: currentPage,
    size: itemsPerPage,
    sort: 'fechaEmision,desc',
  });

  // Enriquecer prescripciones con datos relacionados
  const prescripcionesConDetalles = useMemo(() => {
    return prescripciones.map(presc => {
      const consulta = consultas.find(c => c.id === presc.consultaId);
      const paciente = consulta ? pacientes.find(p => p.id === consulta.pacienteId) : undefined;
      const profesional = consulta ? usuarios.find(u => u.id === consulta.profesionalId) : undefined;
      
      return {
        ...presc,
        consulta,
        paciente,
        profesional,
      };
    });
  }, [prescripciones, consultas, pacientes, usuarios]);

  // Filtrar por término de búsqueda (si se implementa búsqueda en backend)
  const filteredPrescripciones = useMemo(() => {
    if (!debouncedSearchTerm) return prescripcionesConDetalles;
    
    const searchLower = debouncedSearchTerm.toLowerCase();
    return prescripcionesConDetalles.filter(presc => 
      presc.paciente?.nombre.toLowerCase().includes(searchLower) ||
      presc.profesional?.nombre.toLowerCase().includes(searchLower) ||
      presc.items.some(item => item.medicamento.toLowerCase().includes(searchLower))
    );
  }, [prescripcionesConDetalles, debouncedSearchTerm]);

  const totalPages = prescripcionesPage?.totalPages || 0;
  const totalElements = prescripcionesPage?.totalElements || 0;

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-3xl font-bold text-foreground">Prescripciones y Recetas</h1>
          <p className="text-muted-foreground mt-1">Gestión de prescripciones médicas</p>
        </div>
        <Button className="gap-2" onClick={() => navigate('/prescripciones/nuevo')}>
          <Plus className="h-4 w-4" />
          Nueva Prescripción
        </Button>
      </div>

      {/* Filtros */}
      <div className="grid gap-4 md:grid-cols-2">
        <div className="relative">
          <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-muted-foreground" />
          <Input
            placeholder="Buscar por paciente, profesional o medicamento..."
            className="pl-10"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
        </div>
        <Select value={filtroPaciente} onValueChange={(value) => {
          setFiltroPaciente(value);
          setCurrentPage(0);
        }}>
          <SelectTrigger>
            <SelectValue placeholder="Filtrar por paciente" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="todos">Todos los pacientes</SelectItem>
            {pacientes.map(paciente => (
              <SelectItem key={paciente.id} value={paciente.id}>
                {paciente.nombre}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>
      </div>

      {isLoading ? (
        <LoadingCards count={5} />
      ) : error ? (
        <ErrorState
          title="Error al cargar prescripciones"
          message={error instanceof Error ? error.message : 'Ocurrió un error inesperado'}
          onRetry={() => refetch()}
        />
      ) : filteredPrescripciones.length === 0 ? (
        <EmptyState
          icon={Pill}
          title="No hay prescripciones"
          description={
            filtroPaciente !== 'todos' || searchTerm
              ? 'No se encontraron prescripciones con los filtros aplicados'
              : 'Las prescripciones aparecerán aquí cuando se registren'
          }
          actionLabel="Nueva Prescripción"
          onAction={() => navigate('/prescripciones/nuevo')}
        />
      ) : (
        <>
          <div className="grid gap-4">
            {filteredPrescripciones.map((prescripcion) => (
          <Card key={prescripcion.id} className="hover:shadow-lg transition-shadow">
            <CardHeader>
              <div className="flex items-start justify-between">
                <div className="flex items-center gap-3">
                  <div className="h-12 w-12 rounded-full bg-info/10 flex items-center justify-center">
                    <Pill className="h-6 w-6 text-info" />
                  </div>
                  <div>
                    <CardTitle className="text-xl">Prescripción #{prescripcion.id}</CardTitle>
                    <div className="flex items-center gap-4 mt-2 text-sm text-muted-foreground">
                      {prescripcion.paciente && (
                        <div className="flex items-center gap-1">
                          <User className="h-4 w-4" />
                          {prescripcion.paciente.nombre}
                        </div>
                      )}
                      {prescripcion.consulta && (
                        <div className="flex items-center gap-1">
                          <Calendar className="h-4 w-4" />
                          {new Date(prescripcion.consulta.fecha).toLocaleDateString('es-ES')}
                        </div>
                      )}
                    </div>
                  </div>
                </div>
                <Button 
                  variant="outline"
                  onClick={() => navigate(`/prescripciones/${prescripcion.id}`)}
                  className="gap-2"
                >
                  <FileText className="h-4 w-4" />
                  Ver Receta
                </Button>
              </div>
            </CardHeader>
            <CardContent>
              <div className="space-y-3">
                <div>
                  <h4 className="font-semibold text-sm mb-2">Medicamentos Prescritos</h4>
                  <div className="space-y-2">
                    {prescripcion.items.map((item, index) => (
                      <div key={index} className="p-3 bg-accent/50 rounded-lg">
                        <div className="flex items-start justify-between mb-2">
                          <div>
                            <p className="font-medium">{item.medicamento}</p>
                            {item.presentacion && (
                              <p className="text-sm text-muted-foreground">{item.presentacion}</p>
                            )}
                          </div>
                          <Badge variant="outline" className="bg-info/10 text-info border-info/20">
                            {item.duracionDias} días
                          </Badge>
                        </div>
                        <div className="grid grid-cols-2 gap-2 text-sm">
                          <div>
                            <span className="text-muted-foreground">Dosis:</span>
                            <span className="ml-2 font-medium">{item.dosis}</span>
                          </div>
                          <div>
                            <span className="text-muted-foreground">Frecuencia:</span>
                            <span className="ml-2 font-medium">{item.frecuencia}</span>
                          </div>
                        </div>
                        {item.indicaciones && (
                          <p className="text-sm text-muted-foreground mt-2 italic">
                            {item.indicaciones}
                          </p>
                        )}
                      </div>
                    ))}
                  </div>
                </div>
                {prescripcion.profesional && (
                  <div className="pt-3 border-t">
                    <p className="text-sm text-muted-foreground">Prescrito por</p>
                    <p className="font-medium">{prescripcion.profesional.nombre}</p>
                  </div>
                )}
              </div>
            </CardContent>
            </Card>
            ))}
          </div>

          {totalPages > 1 && (
            <Pagination
              currentPage={currentPage + 1}
              totalPages={totalPages}
              onPageChange={(page) => setCurrentPage(page - 1)}
              itemsPerPage={itemsPerPage}
              totalItems={totalElements}
            />
          )}
        </>
      )}
    </div>
  );
}
