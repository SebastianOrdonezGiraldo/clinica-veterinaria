import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Plus, Search, Syringe, Calendar, User, AlertCircle, CheckCircle2 } from 'lucide-react';
import { Button } from '@shared/components/ui/button';
import { Input } from '@shared/components/ui/input';
import { Card, CardContent, CardHeader, CardTitle } from '@shared/components/ui/card';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@shared/components/ui/select';
import { Badge } from '@shared/components/ui/badge';
import { LoadingCards } from '@shared/components/common/LoadingCards';
import { useVacunaciones, useVacunacionesProximas, useVacunacionesVencidas } from '../hooks/useVacunaciones';
import { useAllPacientes } from '@features/pacientes/hooks/usePacientes';
import { useDebounce } from '@shared/hooks/useDebounce';
import { format } from 'date-fns';
import { es } from 'date-fns/locale';

export default function Vacunaciones() {
  const navigate = useNavigate();
  const [searchTerm, setSearchTerm] = useState('');
  const [filtroPaciente, setFiltroPaciente] = useState<string>('todos');
  const [currentPage, setCurrentPage] = useState(0);
  const [tab, setTab] = useState<'todas' | 'proximas' | 'vencidas'>('todas');
  const debouncedSearch = useDebounce(searchTerm, 300);

  const { data: vacunacionesPage, isLoading } = useVacunaciones({
    pacienteId: filtroPaciente !== 'todos' ? filtroPaciente : undefined,
    page: currentPage,
    size: 20,
    sort: 'fechaAplicacion,desc',
  });

  const { data: proximas = [] } = useVacunacionesProximas(30);
  const { data: vencidas = [] } = useVacunacionesVencidas();
  const { pacientes = [] } = useAllPacientes();

  const vacunaciones = vacunacionesPage?.content || [];
  const vacunacionesToShow = tab === 'proximas' ? proximas : tab === 'vencidas' ? vencidas : vacunaciones;

  const filteredVacunaciones = vacunacionesToShow.filter(v => {
    if (!debouncedSearch) return true;
    const search = debouncedSearch.toLowerCase();
    return (
      v.pacienteNombre?.toLowerCase().includes(search) ||
      v.vacunaNombre?.toLowerCase().includes(search) ||
      v.profesionalNombre?.toLowerCase().includes(search)
    );
  });

  if (isLoading && tab === 'todas') {
    return <LoadingCards count={6} />;
  }

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-3xl font-bold text-foreground">Vacunaciones</h1>
          <p className="text-muted-foreground mt-1">Registro y seguimiento de vacunaciones</p>
        </div>
        <Button onClick={() => navigate('/vacunaciones/nueva')} className="gap-2">
          <Plus className="h-4 w-4" />
          Nueva Vacunación
        </Button>
      </div>

      <div className="flex gap-2 border-b">
        <Button
          variant={tab === 'todas' ? 'default' : 'ghost'}
          onClick={() => setTab('todas')}
          className="rounded-b-none"
        >
          Todas
          {vacunacionesPage && (
            <Badge variant="secondary" className="ml-2">
              {vacunacionesPage.totalElements}
            </Badge>
          )}
        </Button>
        <Button
          variant={tab === 'proximas' ? 'default' : 'ghost'}
          onClick={() => setTab('proximas')}
          className="rounded-b-none"
        >
          Próximas a Vencer
          {proximas.length > 0 && (
            <Badge variant="secondary" className="ml-2">
              {proximas.length}
            </Badge>
          )}
        </Button>
        <Button
          variant={tab === 'vencidas' ? 'default' : 'ghost'}
          onClick={() => setTab('vencidas')}
          className="rounded-b-none"
        >
          Vencidas
          {vencidas.length > 0 && (
            <Badge variant="destructive" className="ml-2">
              {vencidas.length}
            </Badge>
          )}
        </Button>
      </div>

      <div className="grid gap-4 md:grid-cols-3">
        <div className="relative md:col-span-2">
          <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-muted-foreground" />
          <Input
            placeholder="Buscar por paciente, vacuna o profesional..."
            className="pl-10"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
        </div>
        {tab === 'todas' && (
          <Select value={filtroPaciente} onValueChange={setFiltroPaciente}>
            <SelectTrigger>
              <SelectValue placeholder="Filtrar por paciente" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="todos">Todos los pacientes</SelectItem>
              {pacientes.map((paciente) => (
                <SelectItem key={paciente.id} value={paciente.id}>
                  {paciente.nombre}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        )}
      </div>

      {filteredVacunaciones.length === 0 ? (
        <Card>
          <CardContent className="flex flex-col items-center justify-center py-12">
            <Syringe className="h-12 w-12 text-muted-foreground mb-4" />
            <p className="text-muted-foreground text-center">
              {searchTerm || filtroPaciente !== 'todos'
                ? 'No se encontraron vacunaciones'
                : tab === 'proximas'
                ? 'No hay vacunaciones próximas a vencer'
                : tab === 'vencidas'
                ? 'No hay vacunaciones vencidas'
                : 'No hay vacunaciones registradas'}
            </p>
          </CardContent>
        </Card>
      ) : (
        <div className="space-y-4">
          {filteredVacunaciones.map((vacunacion) => {
            const fechaAplicacion = new Date(vacunacion.fechaAplicacion);
            const proximaDosis = vacunacion.proximaDosis ? new Date(vacunacion.proximaDosis) : null;
            const hoy = new Date();
            const diasRestantes = proximaDosis
              ? Math.ceil((proximaDosis.getTime() - hoy.getTime()) / (1000 * 60 * 60 * 24))
              : null;
            const estaVencida = proximaDosis && proximaDosis < hoy;

            return (
              <Card key={vacunacion.id} className="hover:shadow-md transition-shadow">
                <CardHeader>
                  <div className="flex justify-between items-start">
                    <div className="flex-1">
                      <CardTitle className="text-lg">{vacunacion.vacunaNombre}</CardTitle>
                      <div className="flex items-center gap-4 mt-2 text-sm text-muted-foreground">
                        <div className="flex items-center gap-1">
                          <User className="h-4 w-4" />
                          {vacunacion.pacienteNombre}
                        </div>
                        {vacunacion.especiePaciente && (
                          <Badge variant="outline">{vacunacion.especiePaciente}</Badge>
                        )}
                      </div>
                    </div>
                    <div className="flex flex-col items-end gap-2">
                      {estaVencida ? (
                        <Badge variant="destructive" className="gap-1">
                          <AlertCircle className="h-3 w-3" />
                          Vencida
                        </Badge>
                      ) : diasRestantes !== null && diasRestantes <= 30 ? (
                        <Badge variant="outline" className="gap-1 text-orange-600 border-orange-600">
                          <AlertCircle className="h-3 w-3" />
                          {diasRestantes} días
                        </Badge>
                      ) : proximaDosis ? (
                        <Badge variant="outline" className="gap-1 text-green-600 border-green-600">
                          <CheckCircle2 className="h-3 w-3" />
                          {diasRestantes} días
                        </Badge>
                      ) : null}
                    </div>
                  </div>
                </CardHeader>
                <CardContent>
                  <div className="grid gap-4 md:grid-cols-2">
                    <div className="space-y-2">
                      <div className="flex items-center gap-2 text-sm">
                        <Calendar className="h-4 w-4 text-muted-foreground" />
                        <span className="text-muted-foreground">Fecha de aplicación:</span>
                        <span className="font-medium">
                          {format(fechaAplicacion, "d 'de' MMMM, yyyy", { locale: es })}
                        </span>
                      </div>
                      <div className="flex items-center gap-2 text-sm">
                        <span className="text-muted-foreground">Dosis:</span>
                        <span className="font-medium">{vacunacion.numeroDosis}</span>
                      </div>
                      {vacunacion.lote && (
                        <div className="flex items-center gap-2 text-sm">
                          <span className="text-muted-foreground">Lote:</span>
                          <span className="font-medium">{vacunacion.lote}</span>
                        </div>
                      )}
                    </div>
                    <div className="space-y-2">
                      {proximaDosis && (
                        <div className="flex items-center gap-2 text-sm">
                          <Calendar className="h-4 w-4 text-muted-foreground" />
                          <span className="text-muted-foreground">Próxima dosis:</span>
                          <span className="font-medium">
                            {format(proximaDosis, "d 'de' MMMM, yyyy", { locale: es })}
                          </span>
                        </div>
                      )}
                      <div className="flex items-center gap-2 text-sm">
                        <User className="h-4 w-4 text-muted-foreground" />
                        <span className="text-muted-foreground">Profesional:</span>
                        <span className="font-medium">{vacunacion.profesionalNombre}</span>
                      </div>
                      {vacunacion.observaciones && (
                        <p className="text-sm text-muted-foreground mt-2">
                          {vacunacion.observaciones}
                        </p>
                      )}
                    </div>
                  </div>
                  <div className="mt-4 flex justify-end">
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={() => navigate(`/pacientes/${vacunacion.pacienteId}`)}
                    >
                      Ver Paciente
                    </Button>
                  </div>
                </CardContent>
              </Card>
            );
          })}
        </div>
      )}

      {tab === 'todas' && vacunacionesPage && vacunacionesPage.totalPages > 1 && (
        <div className="flex items-center justify-center gap-2">
          <Button
            variant="outline"
            onClick={() => setCurrentPage(p => Math.max(0, p - 1))}
            disabled={currentPage === 0}
          >
            Anterior
          </Button>
          <span className="text-sm text-muted-foreground">
            Página {currentPage + 1} de {vacunacionesPage.totalPages}
          </span>
          <Button
            variant="outline"
            onClick={() => setCurrentPage(p => Math.min(vacunacionesPage.totalPages - 1, p + 1))}
            disabled={currentPage >= vacunacionesPage.totalPages - 1}
          >
            Siguiente
          </Button>
        </div>
      )}
    </div>
  );
}

