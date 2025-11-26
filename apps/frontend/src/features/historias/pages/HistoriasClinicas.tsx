import { useState, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import { FileText, Search, Dog } from 'lucide-react';
import { Input } from '@shared/components/ui/input';
import { Card, CardContent, CardHeader, CardTitle } from '@shared/components/ui/card';
import { Badge } from '@shared/components/ui/badge';
import { LoadingCards } from '@shared/components/common/LoadingCards';
import { EmptyState } from '@shared/components/common/EmptyState';
import { ErrorState } from '@shared/components/common/ErrorState';
import { useDebounce } from '@shared/hooks/useDebounce';
import { usePacientes } from '@features/pacientes/hooks/usePacientes';
import { usePropietarios } from '@features/propietarios/hooks/usePropietarios';
import { useConsultas } from '@features/historias/hooks/useConsultas';
import { useQuery } from '@tanstack/react-query';
import { consultaService } from '@features/historias/services/consultaService';

export default function HistoriasClinicas() {
  const navigate = useNavigate();
  const [searchTerm, setSearchTerm] = useState('');
  const debouncedSearchTerm = useDebounce(searchTerm, 500);

  // Cargar pacientes con paginación (cargar todos para esta vista)
  const { pacientes = [], isLoading: isLoadingPacientes, error: errorPacientes, refetch: refetchPacientes } = usePacientes({
    page: 0,
    size: 1000, // Cargar muchos pacientes para esta vista
    sort: 'nombre,asc',
  });

  // Cargar propietarios con paginación
  const { propietarios = [], isLoading: isLoadingPropietarios } = usePropietarios({
    page: 0,
    size: 1000,
    sort: 'nombre,asc',
  });

  // Cargar todas las consultas para contar por paciente
  // Usamos React Query directamente para cachear
  const { data: consultas = [], isLoading: isLoadingConsultas } = useQuery({
    queryKey: ['consultas', 'all'],
    queryFn: () => consultaService.getAll(),
    staleTime: 60000, // Cache por 1 minuto
  });

  const isLoading = isLoadingPacientes || isLoadingPropietarios || isLoadingConsultas;
  const error = errorPacientes;

  const filteredPacientes = useMemo(() => {
    if (!debouncedSearchTerm) return pacientes;
    
    const searchLower = debouncedSearchTerm.toLowerCase();
    return pacientes.filter(p =>
      p.nombre.toLowerCase().includes(searchLower) ||
      propietarios.find(pr => pr.id === p.propietarioId)?.nombre.toLowerCase().includes(searchLower)
    );
  }, [pacientes, propietarios, debouncedSearchTerm]);

  const getConsultasCount = (pacienteId: string) => 
    consultas.filter(c => c.pacienteId === pacienteId).length;

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold text-foreground">Historias Clínicas</h1>
        <p className="text-muted-foreground mt-1">Consulta historias clínicas de pacientes</p>
      </div>

      <div className="relative">
        <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-muted-foreground" />
        <Input
          placeholder="Buscar paciente o propietario..."
          className="pl-10"
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
        />
      </div>

      {isLoading ? (
        <LoadingCards count={6} />
      ) : error ? (
        <ErrorState
          title="Error al cargar historias clínicas"
          message={error instanceof Error ? error.message : 'Ocurrió un error inesperado'}
          onRetry={() => refetchPacientes()}
        />
      ) : (
        <>
          {filteredPacientes.length > 0 ? (
            <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
              {filteredPacientes.map((paciente) => {
                const propietario = propietarios.find(p => p.id === paciente.propietarioId);
                const consultasCount = getConsultasCount(paciente.id);
                
                return (
                  <Card
                    key={paciente.id}
                    className="cursor-pointer hover:shadow-lg transition-shadow"
                    onClick={() => navigate(`/historias/${paciente.id}`)}
                  >
                    <CardHeader className="pb-3">
                      <div className="flex items-start justify-between">
                        <div className="flex items-center gap-3">
                          <div className="h-12 w-12 rounded-full bg-primary/10 flex items-center justify-center">
                            <Dog className="h-6 w-6 text-primary" />
                          </div>
                          <div>
                            <CardTitle className="text-lg">{paciente.nombre}</CardTitle>
                            <p className="text-sm text-muted-foreground">{paciente.raza || 'Sin raza'}</p>
                          </div>
                        </div>
                        <Badge variant="outline" className="bg-secondary/10 text-secondary border-secondary/20">
                          {paciente.especie}
                        </Badge>
                      </div>
                    </CardHeader>
                    <CardContent className="space-y-2">
                      <div className="flex items-center gap-2 text-sm">
                        <FileText className="h-4 w-4 text-muted-foreground" />
                        <span className="font-medium">{consultasCount} {consultasCount === 1 ? 'consulta' : 'consultas'} registrada{consultasCount !== 1 ? 's' : ''}</span>
                      </div>
                      <div className="pt-2 border-t">
                        <p className="text-xs text-muted-foreground">Propietario</p>
                        <p className="text-sm font-medium">{propietario?.nombre || 'Sin propietario'}</p>
                      </div>
                    </CardContent>
                  </Card>
                );
              })}
            </div>
          ) : (
            <EmptyState
              icon={Search}
              title={searchTerm ? 'No se encontraron resultados' : 'No hay pacientes registrados'}
              description={
                searchTerm 
                  ? `No se encontraron pacientes que coincidan con "${searchTerm}".`
                  : 'No hay pacientes registrados en el sistema.'
              }
            />
          )}
        </>
      )}
    </div>
  );
}
