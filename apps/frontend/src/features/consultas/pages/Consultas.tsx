import { useState, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import { FileText, Search, Calendar, User, Plus, Filter } from 'lucide-react';
import { Input } from '@shared/components/ui/input';
import { Card, CardContent, CardHeader, CardTitle } from '@shared/components/ui/card';
import { Badge } from '@shared/components/ui/badge';
import { Button } from '@shared/components/ui/button';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@shared/components/ui/select';
import { LoadingCards } from '@shared/components/common/LoadingCards';
import { EmptyState } from '@shared/components/common/EmptyState';
import { ErrorState } from '@shared/components/common/ErrorState';
import { useDebounce } from '@shared/hooks/useDebounce';
import { useConsultas } from '@features/historias/hooks/useConsultas';
import { useAuth } from '@core/auth/AuthContext';
import { Consulta } from '@core/types';
import { format } from 'date-fns';
import { es } from 'date-fns/locale';

export default function Consultas() {
  const navigate = useNavigate();
  const { user } = useAuth();
  const [searchTerm, setSearchTerm] = useState('');
  const [filtroProfesional, setFiltroProfesional] = useState<string>('todos');
  const [currentPage, setCurrentPage] = useState(0);
  const debouncedSearchTerm = useDebounce(searchTerm, 500);
  const itemsPerPage = 20;

  // Filtrar por profesional si es VET (solo sus consultas)
  const profesionalId = user?.rol === 'VET' ? user.id : filtroProfesional !== 'todos' ? filtroProfesional : undefined;

  const { consultasPage, isLoading, error, refetch } = useConsultas({
    profesionalId,
    page: currentPage,
    size: itemsPerPage,
    sort: 'fecha,desc',
  });

  const consultas = consultasPage?.content || [];

  const filteredConsultas = useMemo(() => {
    if (!debouncedSearchTerm) return consultas;
    
    const searchLower = debouncedSearchTerm.toLowerCase();
    return consultas.filter(c =>
      c.pacienteNombre?.toLowerCase().includes(searchLower) ||
      c.profesionalNombre?.toLowerCase().includes(searchLower) ||
      c.diagnostico?.toLowerCase().includes(searchLower)
    );
  }, [consultas, debouncedSearchTerm]);

  if (isLoading) {
    return (
      <div className="space-y-6">
        <div>
          <h1 className="text-3xl font-bold text-foreground">Consultas</h1>
          <p className="text-muted-foreground mt-1">Gestión de consultas médicas</p>
        </div>
        <LoadingCards count={6} />
      </div>
    );
  }

  if (error) {
    return (
      <div className="space-y-6">
        <div>
          <h1 className="text-3xl font-bold text-foreground">Consultas</h1>
          <p className="text-muted-foreground mt-1">Gestión de consultas médicas</p>
        </div>
        <ErrorState
          title="Error al cargar consultas"
          message={error instanceof Error ? error.message : 'Ocurrió un error inesperado'}
          onRetry={() => refetch()}
        />
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-3xl font-bold text-foreground">Consultas</h1>
          <p className="text-muted-foreground mt-1">Gestión de consultas médicas</p>
        </div>
        {user?.rol === 'VET' || user?.rol === 'ADMIN' ? (
          <Button onClick={() => navigate('/historias')} className="gap-2">
            <Plus className="h-4 w-4" />
            Nueva Consulta
          </Button>
        ) : null}
      </div>

      <div className="flex gap-4 flex-col sm:flex-row">
        <div className="relative flex-1">
          <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-muted-foreground" />
          <Input
            placeholder="Buscar por paciente, profesional o diagnóstico..."
            className="pl-10"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
        </div>
        {user?.rol === 'ADMIN' && (
          <Select value={filtroProfesional} onValueChange={setFiltroProfesional}>
            <SelectTrigger className="w-full sm:w-[200px]">
              <Filter className="h-4 w-4 mr-2" />
              <SelectValue placeholder="Profesional" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="todos">Todos</SelectItem>
              {/* Aquí podrías agregar una lista de profesionales */}
            </SelectContent>
          </Select>
        )}
      </div>

      {filteredConsultas.length > 0 ? (
        <div className="space-y-4">
          <div className="text-sm text-muted-foreground">
            Mostrando {filteredConsultas.length} de {consultasPage?.totalElements || 0} consultas
          </div>
          <div className="space-y-3">
            {filteredConsultas.map((consulta) => (
              <Card
                key={consulta.id}
                className="cursor-pointer hover:shadow-lg transition-shadow"
                onClick={() => navigate(`/historias/${consulta.id}`)}
              >
                <CardContent className="p-4">
                  <div className="flex items-start justify-between gap-4">
                    <div className="flex-1 min-w-0">
                      <div className="flex items-center gap-3 mb-2">
                        <div className="h-10 w-10 rounded-full bg-primary/10 flex items-center justify-center flex-shrink-0">
                          <FileText className="h-5 w-5 text-primary" />
                        </div>
                        <div className="flex-1 min-w-0">
                          <h3 className="font-semibold text-lg truncate">
                            {consulta.pacienteNombre || 'Paciente desconocido'}
                          </h3>
                          <div className="flex items-center gap-2 mt-1">
                            <Calendar className="h-3 w-3 text-muted-foreground" />
                            <span className="text-sm text-muted-foreground">
                              {format(new Date(consulta.fecha), "d 'de' MMMM 'de' yyyy 'a las' HH:mm", { locale: es })}
                            </span>
                          </div>
                        </div>
                      </div>

                      {consulta.diagnostico && (
                        <p className="text-sm text-muted-foreground mt-2 line-clamp-2">
                          <span className="font-medium">Diagnóstico:</span> {consulta.diagnostico}
                        </p>
                      )}

                      <div className="flex items-center gap-4 mt-3 text-xs text-muted-foreground">
                        {consulta.profesionalNombre && (
                          <div className="flex items-center gap-1">
                            <User className="h-3 w-3" />
                            <span>{consulta.profesionalNombre}</span>
                          </div>
                        )}
                        {consulta.frecuenciaCardiaca && (
                          <span>FC: {consulta.frecuenciaCardiaca} lpm</span>
                        )}
                        {consulta.temperatura && (
                          <span>T: {consulta.temperatura}°C</span>
                        )}
                        {consulta.pesoKg && (
                          <span>Peso: {consulta.pesoKg} kg</span>
                        )}
                      </div>
                    </div>
                  </div>
                </CardContent>
              </Card>
            ))}
          </div>

          {/* Paginación */}
          {consultasPage && consultasPage.totalPages > 1 && (
            <div className="flex items-center justify-between pt-4">
              <div className="text-sm text-muted-foreground">
                Página {currentPage + 1} de {consultasPage.totalPages}
              </div>
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
                  onClick={() => setCurrentPage(prev => Math.min(consultasPage.totalPages - 1, prev + 1))}
                  disabled={currentPage === consultasPage.totalPages - 1}
                >
                  Siguiente
                </Button>
              </div>
            </div>
          )}
        </div>
      ) : (
        <EmptyState
          icon={FileText}
          title={searchTerm ? 'No se encontraron resultados' : 'No hay consultas registradas'}
          description={
            searchTerm
              ? `No se encontraron consultas que coincidan con "${searchTerm}".`
              : 'No hay consultas registradas en el sistema.'
          }
        />
      )}
    </div>
  );
}

