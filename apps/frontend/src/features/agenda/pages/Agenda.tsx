import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Calendar as CalendarIcon, Clock, Plus, Filter, AlertCircle, ChevronLeft, ChevronRight } from 'lucide-react';
import { Button } from '@shared/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@shared/components/ui/card';
import { Badge } from '@shared/components/ui/badge';
import { Calendar } from '@shared/components/ui/calendar';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@shared/components/ui/select';
import { Popover, PopoverContent, PopoverTrigger } from '@shared/components/ui/popover';
import { format, startOfDay, endOfDay } from 'date-fns';
import { es } from 'date-fns/locale';
import { citaService, EstadoCita } from '@features/agenda/services/citaService';
import { usuarioService } from '@features/usuarios/services/usuarioService';
import { Cita, Usuario, PageResponse } from '@core/types';
import { toast } from 'sonner';

const statusColors = {
  CONFIRMADA: 'bg-status-confirmed/10 text-status-confirmed border-status-confirmed/20',
  PENDIENTE: 'bg-status-pending/10 text-status-pending border-status-pending/20',
  EN_PROCESO: 'bg-blue-500/10 text-blue-500 border-blue-500/20',
  COMPLETADA: 'bg-status-completed/10 text-status-completed border-status-completed/20',
  CANCELADA: 'bg-status-cancelled/10 text-status-cancelled border-status-cancelled/20',
  ATENDIDA: 'bg-status-completed/10 text-status-completed border-status-completed/20', // Alias para compatibilidad
};

/**
 * Componente Agenda - Gestión de citas con paginación backend
 * 
 * MEJORAS IMPLEMENTADAS:
 * - ✓ Paginación del lado del servidor (no carga todas las citas)
 * - ✓ Filtros por fecha, estado y profesional en backend
 * - ✓ Solo carga citas del día seleccionado (eficiente)
 * - ✓ Las relaciones (paciente, propietario) vienen del backend
 * 
 * PATRONES APLICADOS:
 * - Observer Pattern: useEffect reacciona a cambios de fecha y filtros
 * - Strategy Pattern: Backend selecciona query apropiado
 */
export default function Agenda() {
  const navigate = useNavigate();
  
  // ESTADO: Fecha y filtros
  const [selectedDate, setSelectedDate] = useState<Date>(new Date());
  const [filtroVet, setFiltroVet] = useState<string>('todos');
  const [filtroEstado, setFiltroEstado] = useState<EstadoCita | 'todos'>('todos');
  
  // ESTADO: Paginación backend
  const [citasPage, setCitasPage] = useState<PageResponse<Cita> | null>(null);
  const [currentPage, setCurrentPage] = useState(0); // 0-indexed para backend
  const itemsPerPage = 20;
  
  // ESTADO: Veterinarios para filtro
  const [veterinarios, setVeterinarios] = useState<Usuario[]>([]);
  
  // ESTADO: UI
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // EFECTO: Cargar veterinarios una sola vez
  useEffect(() => {
    loadVeterinarios();
  }, []);

  // EFECTO: Cargar citas cuando cambian fecha, filtros o página
  // PATRÓN: Observer - Reacciona a cambios de dependencias
  useEffect(() => {
    loadCitas();
  }, [selectedDate, filtroVet, filtroEstado, currentPage]);

  /**
   * Carga la lista de veterinarios para el filtro
   * Se ejecuta una sola vez al montar el componente
   */
  const loadVeterinarios = async () => {
    try {
      const usuarios = await usuarioService.getAll();
      setVeterinarios(usuarios.filter(u => u.rol === 'VET'));
    } catch (error) {
      console.error('Error al cargar veterinarios:', error);
      // No es crítico, solo afecta el filtro
    }
  };

  /**
   * Carga citas del día seleccionado con filtros
   * 
   * VENTAJAS:
   * - Solo carga citas del día (eficiente)
   * - Filtros aplicados en BD (rápido)
   * - Las relaciones vienen incluidas desde el backend
   */
  const loadCitas = async () => {
    try {
      setIsLoading(true);
      setError(null);
      
      // Calcular rango de fechas del día seleccionado
      const fechaInicio = startOfDay(selectedDate).toISOString();
      const fechaFin = endOfDay(selectedDate).toISOString();
      
      // Construir parámetros de búsqueda
      const searchParams = {
        fechaInicio,
        fechaFin,
        profesionalId: filtroVet !== 'todos' ? filtroVet : undefined,
        estado: filtroEstado !== 'todos' ? filtroEstado : undefined,
        page: currentPage,
        size: itemsPerPage,
        sort: 'fecha,asc',
      };
      
      const result = await citaService.searchWithFilters(searchParams);
      setCitasPage(result);
    } catch (error: any) {
      console.error('Error al cargar citas:', error);
      const errorMessage = error.response?.data?.mensaje || error.response?.data?.message || 'Error al cargar citas';
      setError(errorMessage);
      toast.error(errorMessage);
    } finally {
      setIsLoading(false);
    }
  };

  /**
   * Maneja el cambio de fecha del calendario
   * Resetea la página a 0 cuando cambia la fecha
   */
  const handleDateChange = (date: Date | undefined) => {
    if (date) {
      setSelectedDate(date);
      setCurrentPage(0); // Resetear paginación
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-3xl font-bold text-foreground">Agenda</h1>
          <p className="text-muted-foreground mt-1">Gestión de citas y calendario</p>
        </div>
        <Button className="gap-2" onClick={() => navigate('/agenda/nuevo')}>
          <Plus className="h-4 w-4" />
          Nueva Cita
        </Button>
      </div>

      <Card className="mb-6">
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Filter className="h-5 w-5 text-primary" />
            Filtros
          </CardTitle>
        </CardHeader>
        <CardContent>
          <div className="grid gap-4 md:grid-cols-3">
            <div>
              <label className="text-sm font-medium mb-2 block">Fecha</label>
              <Popover>
                <PopoverTrigger asChild>
                  <Button variant="outline" className="w-full justify-start">
                    <CalendarIcon className="mr-2 h-4 w-4" />
                    {format(selectedDate, 'PPP', { locale: es })}
                  </Button>
                </PopoverTrigger>
                <PopoverContent className="w-auto p-0">
                  <Calendar
                    mode="single"
                    selected={selectedDate}
                    onSelect={handleDateChange}
                    initialFocus
                  />
                </PopoverContent>
              </Popover>
            </div>
            <div>
              <label className="text-sm font-medium mb-2 block">Veterinario</label>
              <Select value={filtroVet} onValueChange={setFiltroVet}>
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="todos">Todos</SelectItem>
                  {veterinarios.map(vet => (
                    <SelectItem key={vet.id} value={vet.id}>{vet.nombre}</SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
            <div>
              <label className="text-sm font-medium mb-2 block">Estado</label>
              <Select value={filtroEstado} onValueChange={(value) => {
                setFiltroEstado(value as EstadoCita | 'todos');
                setCurrentPage(0); // Resetear paginación al cambiar filtro
              }}>
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="todos">Todos</SelectItem>
                  <SelectItem value="PENDIENTE">Pendiente</SelectItem>
                  <SelectItem value="CONFIRMADA">Confirmada</SelectItem>
                  <SelectItem value="EN_PROCESO">En Proceso</SelectItem>
                  <SelectItem value="COMPLETADA">Completada</SelectItem>
                  <SelectItem value="CANCELADA">Cancelada</SelectItem>
                </SelectContent>
              </Select>
            </div>
          </div>
        </CardContent>
      </Card>

      <div className="grid gap-6 lg:grid-cols-3">
        <Card className="lg:col-span-1">
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <CalendarIcon className="h-5 w-5 text-primary" />
              Calendario
            </CardTitle>
          </CardHeader>
          <CardContent>
            <Calendar
              mode="single"
              selected={selectedDate}
              onSelect={handleDateChange}
              className="rounded-md border"
            />
          </CardContent>
        </Card>

        <Card className="lg:col-span-2">
          <CardHeader>
            <CardTitle className="flex items-center justify-between">
              <div className="flex items-center gap-2">
                <Clock className="h-5 w-5 text-primary" />
                Citas del Día ({citasPage?.totalElements || 0})
              </div>
              {citasPage && citasPage.totalPages > 1 && (
                <div className="flex items-center gap-2">
                  <Button
                    variant="outline"
                    size="sm"
                    onClick={() => setCurrentPage(prev => Math.max(0, prev - 1))}
                    disabled={currentPage === 0}
                  >
                    <ChevronLeft className="h-4 w-4" />
                  </Button>
                  <span className="text-sm text-muted-foreground">
                    Página {currentPage + 1} de {citasPage.totalPages}
                  </span>
                  <Button
                    variant="outline"
                    size="sm"
                    onClick={() => setCurrentPage(prev => Math.min(citasPage.totalPages - 1, prev + 1))}
                    disabled={currentPage === citasPage.totalPages - 1}
                  >
                    <ChevronRight className="h-4 w-4" />
                  </Button>
                </div>
              )}
            </CardTitle>
          </CardHeader>
          <CardContent>
            {isLoading ? (
              <div className="text-center py-12">
                <p className="text-muted-foreground">Cargando citas...</p>
              </div>
            ) : error ? (
              <div className="text-center py-12">
                <AlertCircle className="h-12 w-12 text-destructive mx-auto mb-4" />
                <h3 className="text-lg font-medium text-foreground mb-2">Error al cargar datos</h3>
                <p className="text-sm text-muted-foreground mb-4">{error}</p>
                <Button onClick={loadCitas} variant="outline">
                  Reintentar
                </Button>
              </div>
            ) : citasPage && citasPage.content.length > 0 ? (
              <div className="space-y-3">
                {citasPage.content.map((cita) => (
                <div
                  key={cita.id}
                  className="flex items-center gap-4 p-4 rounded-lg border border-border hover:bg-accent/50 transition-colors cursor-pointer"
                  onClick={() => navigate(`/agenda/${cita.id}`)}
                >
                  <div className="flex flex-col items-center justify-center w-20 h-20 rounded-lg bg-primary/10 flex-shrink-0">
                    <span className="text-xs text-muted-foreground">Hora</span>
                    <span className="text-lg font-bold text-primary">
                      {new Date(cita.fecha).toLocaleTimeString('es-ES', { hour: '2-digit', minute: '2-digit' })}
                    </span>
                  </div>
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center gap-2 mb-1">
                      <h4 className="font-semibold text-foreground">{cita.paciente?.nombre || 'N/A'}</h4>
                      <Badge variant="outline" className="text-xs">
                        {cita.paciente?.especie || 'N/A'}
                      </Badge>
                    </div>
                    <p className="text-sm text-muted-foreground truncate">
                      Propietario: {cita.propietario?.nombre || 'N/A'}
                    </p>
                    {cita.motivo && (
                      <p className="text-sm text-muted-foreground mt-1">{cita.motivo}</p>
                    )}
                  </div>
                  <Badge className={statusColors[cita.estado as keyof typeof statusColors]}>
                    {cita.estado.replace(/_/g, ' ')}
                  </Badge>
                </div>
                ))}
              </div>
            ) : (
              <div className="text-center py-12">
                <Clock className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
                <h3 className="text-lg font-medium text-foreground">No hay citas para esta fecha</h3>
                <p className="text-muted-foreground mt-1">Selecciona otra fecha o ajusta los filtros</p>
              </div>
            )}
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
