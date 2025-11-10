import { useState, useEffect, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import { FileText, Search, Dog } from 'lucide-react';
import { Input } from '@shared/components/ui/input';
import { Card, CardContent, CardHeader, CardTitle } from '@shared/components/ui/card';
import { Badge } from '@shared/components/ui/badge';
import { LoadingCards } from '@shared/components/common/LoadingCards';
import { toast } from 'sonner';
import { pacienteService } from '@features/pacientes/services/pacienteService';
import { propietarioService } from '@features/propietarios/services/propietarioService';
import { consultaService } from '@features/historias/services/consultaService';
import { Paciente, Propietario, Consulta } from '@core/types';

export default function HistoriasClinicas() {
  const navigate = useNavigate();
  const [searchTerm, setSearchTerm] = useState('');
  const [pacientes, setPacientes] = useState<Paciente[]>([]);
  const [propietarios, setPropietarios] = useState<Propietario[]>([]);
  const [consultas, setConsultas] = useState<Consulta[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      setIsLoading(true);
      setError(null);
      
      // Cargar datos en paralelo, pero manejar errores individualmente
      const results = await Promise.allSettled([
        pacienteService.getAll(),
        propietarioService.getAll(),
        consultaService.getAll(),
      ]);

      // Procesar resultados
      if (results[0].status === 'fulfilled') {
        setPacientes(results[0].value);
      } else {
        console.error('Error al cargar pacientes:', results[0].reason);
        const errorMessage = results[0].reason?.response?.data?.message || 'Error al cargar pacientes';
        toast.error(errorMessage);
      }

      if (results[1].status === 'fulfilled') {
        setPropietarios(results[1].value);
      } else {
        console.error('Error al cargar propietarios:', results[1].reason);
        // No mostrar toast para propietarios, solo log
      }

      if (results[2].status === 'fulfilled') {
        setConsultas(results[2].value);
      } else {
        console.error('Error al cargar consultas:', results[2].reason);
        // No mostrar toast para consultas, solo log
      }

      // Si todos fallaron, mostrar error general
      if (results.every(r => r.status === 'rejected')) {
        setError('No se pudieron cargar los datos. Por favor, intenta recargar la página.');
        toast.error('Error al cargar las historias clínicas');
      }
    } catch (error: any) {
      console.error('Error inesperado al cargar datos:', error);
      const errorMessage = error?.response?.data?.message || 'Error inesperado al cargar las historias clínicas';
      setError(errorMessage);
      toast.error(errorMessage);
    } finally {
      setIsLoading(false);
    }
  };

  const filteredPacientes = useMemo(() => {
    return pacientes.filter(p =>
      p.nombre.toLowerCase().includes(searchTerm.toLowerCase()) ||
      propietarios.find(pr => pr.id === p.propietarioId)?.nombre.toLowerCase().includes(searchTerm.toLowerCase())
    );
  }, [pacientes, propietarios, searchTerm]);

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
        <Card className="border-destructive">
          <CardContent className="flex flex-col items-center justify-center py-16">
            <div className="rounded-full bg-destructive/10 p-4 mb-4">
              <FileText className="h-8 w-8 text-destructive" />
            </div>
            <h3 className="text-lg font-semibold text-foreground mb-2">Error al cargar datos</h3>
            <p className="text-sm text-muted-foreground text-center max-w-sm mb-4">{error}</p>
            <Button onClick={loadData} variant="outline">
              Reintentar
            </Button>
          </CardContent>
        </Card>
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
            <Card className="border-dashed">
              <CardContent className="flex flex-col items-center justify-center py-16">
                <div className="rounded-full bg-muted p-4 mb-4">
                  <Search className="h-8 w-8 text-muted-foreground" />
                </div>
                <h3 className="text-lg font-semibold text-foreground mb-2">
                  {searchTerm ? 'No se encontraron resultados' : 'No hay pacientes registrados'}
                </h3>
                <p className="text-sm text-muted-foreground text-center max-w-sm">
                  {searchTerm 
                    ? `No se encontraron pacientes que coincidan con "${searchTerm}".`
                    : 'No hay pacientes registrados en el sistema.'}
                </p>
              </CardContent>
            </Card>
          )}
        </>
      )}
    </div>
  );
}
