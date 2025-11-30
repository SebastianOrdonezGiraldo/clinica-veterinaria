import { useState, useEffect, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import { Calendar as CalendarIcon, Clock, Plus, Filter, AlertCircle, ChevronLeft, ChevronRight, Search, CheckCircle, XCircle, Eye, MoreVertical } from 'lucide-react';
import { Button } from '@shared/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@shared/components/ui/card';
import { Badge } from '@shared/components/ui/badge';
import { Calendar } from '@shared/components/ui/calendar';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@shared/components/ui/select';
import { Popover, PopoverContent, PopoverTrigger } from '@shared/components/ui/popover';
import { Input } from '@shared/components/ui/input';
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger } from '@shared/components/ui/dropdown-menu';
import { format, startOfDay, endOfDay, startOfMonth, endOfMonth, startOfWeek, endOfWeek, subWeeks, addWeeks } from 'date-fns';
import { es } from 'date-fns/locale';
import { citaService, EstadoCita } from '@features/agenda/services/citaService';
import { usuarioService } from '@features/usuarios/services/usuarioService';
import { Cita, Usuario, PageResponse } from '@core/types';
import { toast } from 'sonner';
import { useLogger } from '@shared/hooks/useLogger';
import { useApiError } from '@shared/hooks/useApiError';
import AgendaCalendar from '@features/agenda/components/AgendaCalendar';
import DisponibilidadProfesionales from '@features/agenda/components/DisponibilidadProfesionales';

const statusColors = {
  CONFIRMADA: 'bg-status-confirmed/10 text-status-confirmed border-status-confirmed/20',
  PENDIENTE: 'bg-status-pending/10 text-status-pending border-status-pending/20',
  EN_PROCESO: 'bg-blue-500/10 text-blue-500 border-blue-500/20',
  COMPLETADA: 'bg-status-completed/10 text-status-completed border-status-completed/20',
  CANCELADA: 'bg-status-cancelled/10 text-status-cancelled border-status-cancelled/20',
  ATENDIDA: 'bg-status-completed/10 text-status-completed border-status-completed/20',
};

/**
 * Componente Agenda - Gestión de citas con mejoras UX
 * 
 * MEJORAS IMPLEMENTADAS:
 * - ✓ Paginación del lado del servidor
 * - ✓ Filtros por fecha, estado y profesional
 * - ✓ Calendario mejorado con vistas mensual/semanal/diaria
 * - ✓ Estadísticas del día (resumen por estado)
 * - ✓ Acciones rápidas en cada cita
 * - ✓ Búsqueda rápida por nombre
 * - ✓ Vista de disponibilidad de profesionales
 */
export default function Agenda() {
  const logger = useLogger('Agenda');
  const { handleError, showSuccess } = useApiError();
  const navigate = useNavigate();
  
  // ESTADO: Fecha y filtros
  const [selectedDate, setSelectedDate] = useState<Date>(new Date());
  const [filtroVet, setFiltroVet] = useState<string>('todos');
  const [filtroEstado, setFiltroEstado] = useState<EstadoCita | 'todos'>('todos');
  const [searchQuery, setSearchQuery] = useState<string>('');
  
  // ESTADO: Paginación backend
  const [citasPage, setCitasPage] = useState<PageResponse<Cita> | null>(null);
  const [currentPage, setCurrentPage] = useState(0);
  const itemsPerPage = 20;
  
  // ESTADO: Veterinarios para filtro
  const [veterinarios, setVeterinarios] = useState<Usuario[]>([]);
  
  // ESTADO: Citas para el calendario (rango amplio)
  const [citasCalendario, setCitasCalendario] = useState<Cita[]>([]);
  const [isLoadingCalendar, setIsLoadingCalendar] = useState(false);
  
  // ESTADO: UI
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [updatingCitaId, setUpdatingCitaId] = useState<string | null>(null);

  // EFECTO: Cargar veterinarios una sola vez
  useEffect(() => {
    loadVeterinarios();
  }, []);

  // EFECTO: Cargar citas para el calendario (rango amplio: 2 semanas antes y después)
  useEffect(() => {
    loadCitasCalendario();
  }, [selectedDate, filtroVet, filtroEstado]);

  // EFECTO: Cargar citas cuando cambian fecha, filtros o página
  useEffect(() => {
    loadCitas();
  }, [selectedDate, filtroVet, filtroEstado, currentPage]);

  /**
   * Carga citas para el calendario (rango amplio para vistas mensual/semanal/diaria)
   */
  const loadCitasCalendario = async () => {
    try {
      setIsLoadingCalendar(true);
      // Cargar 2 semanas antes y 2 semanas después de la fecha seleccionada
      const inicioRango = startOfDay(subWeeks(selectedDate, 2));
      const finRango = endOfDay(addWeeks(selectedDate, 2));
      
      const searchParams = {
        fechaInicio: inicioRango.toISOString(),
        fechaFin: finRango.toISOString(),
        profesionalId: filtroVet !== 'todos' ? filtroVet : undefined,
        estado: filtroEstado !== 'todos' ? filtroEstado : undefined,
        page: 0,
        size: 1000, // Cargar todas las citas del rango
        sort: 'fecha,asc',
      };
      
      const result = await citaService.searchWithFilters(searchParams);
      setCitasCalendario(result.content);
    } catch (error) {
      logger.warn('Error al cargar citas para calendario', {
        action: 'loadCitasCalendario',
      });
    } finally {
      setIsLoadingCalendar(false);
    }
  };

  /**
   * Carga la lista de veterinarios para el filtro
   */
  const loadVeterinarios = async () => {
    try {
      const veterinariosData = await usuarioService.getVeterinarios();
      setVeterinarios(veterinariosData);
    } catch (error) {
      logger.warn('Error al cargar veterinarios para filtro', {
        action: 'loadVeterinarios',
      });
    }
  };

  /**
   * Carga citas del día seleccionado con filtros
   */
  const loadCitas = async () => {
    try {
      setIsLoading(true);
      setError(null);
      
      const fechaInicio = startOfDay(selectedDate).toISOString();
      const fechaFin = endOfDay(selectedDate).toISOString();
      
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
      logger.error('Error al cargar citas de la agenda', error, {
        action: 'loadCitas',
        fecha: selectedDate.toISOString(),
        filtroVet,
        filtroEstado,
        page: currentPage,
      });
      const errorMessage = error.response?.data?.mensaje || error.response?.data?.message || 'Error al cargar citas';
      setError(errorMessage);
      toast.error(errorMessage);
    } finally {
      setIsLoading(false);
    }
  };

  /**
   * Maneja el cambio de estado de una cita
   */
  const handleUpdateEstado = async (citaId: string, nuevoEstado: EstadoCita) => {
    try {
      setUpdatingCitaId(citaId);
      await citaService.updateEstado(citaId, nuevoEstado);
      showSuccess(`Cita ${nuevoEstado === 'CONFIRMADA' ? 'confirmada' : nuevoEstado === 'CANCELADA' ? 'cancelada' : 'actualizada'} exitosamente`);
      await loadCitas();
      await loadCitasCalendario(); // Actualizar calendario
    } catch (error: any) {
      handleError(error, 'Error al actualizar el estado de la cita');
    } finally {
      setUpdatingCitaId(null);
    }
  };

  /**
   * Maneja el cambio de fecha del calendario
   */
  const handleDateChange = (date: Date | undefined) => {
    if (date) {
      setSelectedDate(date);
      setCurrentPage(0);
    }
  };

  /**
   * Función para marcar días con citas en el calendario del filtro
   */
  const fechasConCitas = useMemo(() => {
    const fechas = new Set<string>();
    citasCalendario.forEach(cita => {
      const fecha = new Date(cita.fecha);
      const fechaStr = format(fecha, 'yyyy-MM-dd');
      fechas.add(fechaStr);
    });
    return fechas;
  }, [citasCalendario]);

  const modifiers = {
    hasAppointments: (date: Date) => {
      const fechaStr = format(date, 'yyyy-MM-dd');
      return fechasConCitas.has(fechaStr);
    },
  };

  const modifiersClassNames = {
    hasAppointments: 'bg-primary/20 border-primary border-2 font-semibold',
  };

  /**
   * Maneja el click en una cita del calendario
   */
  const handleCitaClick = (cita: Cita) => {
    navigate(`/agenda/${cita.id}`);
  };

  /**
   * Maneja el drag & drop de una cita (cambiar fecha/hora)
   */
  const handleCitaDrag = async (citaId: string, nuevaFecha: Date) => {
    try {
      const cita = citasCalendario.find(c => c.id === citaId);
      if (!cita) return;

      // Actualizar la cita con la nueva fecha
      await citaService.update(citaId, {
        ...cita,
        fecha: nuevaFecha.toISOString(),
        pacienteId: cita.pacienteId,
        propietarioId: cita.propietarioId,
        profesionalId: cita.profesionalId,
        motivo: cita.motivo || '',
      });

      showSuccess('Cita movida exitosamente');
      await loadCitas();
      await loadCitasCalendario();
    } catch (error: any) {
      handleError(error, 'Error al mover la cita');
    }
  };

  /**
   * Filtrar citas por búsqueda
   */
  const citasFiltradas = useMemo(() => {
    if (!citasPage?.content || !searchQuery.trim()) {
      return citasPage?.content || [];
    }
    
    const query = searchQuery.toLowerCase().trim();
    return citasPage.content.filter(cita => 
      (cita.pacienteNombre?.toLowerCase().includes(query)) ||
      (cita.propietarioNombre?.toLowerCase().includes(query)) ||
      (cita.motivo?.toLowerCase().includes(query))
    );
  }, [citasPage?.content, searchQuery]);

  /**
   * Calcular estadísticas del día
   */
  const estadisticas = useMemo(() => {
    const citas = citasPage?.content || [];
    return {
      total: citas.length,
      pendientes: citas.filter(c => c.estado === 'PENDIENTE').length,
      confirmadas: citas.filter(c => c.estado === 'CONFIRMADA').length,
      atendidas: citas.filter(c => c.estado === 'ATENDIDA').length,
      canceladas: citas.filter(c => c.estado === 'CANCELADA').length,
    };
  }, [citasPage?.content]);

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

      {/* Estadísticas del día */}
      <div className="grid gap-4 md:grid-cols-5">
        <Card>
          <CardContent className="pt-6">
            <div className="text-center">
              <div className="text-2xl font-bold text-foreground">{estadisticas.total}</div>
              <div className="text-sm text-muted-foreground mt-1">Total</div>
            </div>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="pt-6">
            <div className="text-center">
              <div className="text-2xl font-bold text-status-pending">{estadisticas.pendientes}</div>
              <div className="text-sm text-muted-foreground mt-1">Pendientes</div>
            </div>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="pt-6">
            <div className="text-center">
              <div className="text-2xl font-bold text-status-confirmed">{estadisticas.confirmadas}</div>
              <div className="text-sm text-muted-foreground mt-1">Confirmadas</div>
            </div>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="pt-6">
            <div className="text-center">
              <div className="text-2xl font-bold text-status-completed">{estadisticas.atendidas}</div>
              <div className="text-sm text-muted-foreground mt-1">Atendidas</div>
            </div>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="pt-6">
            <div className="text-center">
              <div className="text-2xl font-bold text-status-cancelled">{estadisticas.canceladas}</div>
              <div className="text-sm text-muted-foreground mt-1">Canceladas</div>
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Filtros */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Filter className="h-5 w-5 text-primary" />
            Filtros
          </CardTitle>
        </CardHeader>
        <CardContent>
          <div className="grid gap-4 md:grid-cols-4">
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
                    modifiers={modifiers}
                    modifiersClassNames={modifiersClassNames}
                    initialFocus
                  />
                </PopoverContent>
              </Popover>
            </div>
            <div>
              <label className="text-sm font-medium mb-2 block">Búsqueda</label>
              <div className="relative">
                <Search className="absolute left-2 top-2.5 h-4 w-4 text-muted-foreground" />
                <Input
                  placeholder="Buscar por nombre..."
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                  className="pl-8"
                />
              </div>
            </div>
            <div>
              <label className="text-sm font-medium mb-2 block">Veterinario</label>
              <Select value={filtroVet} onValueChange={(value) => {
                setFiltroVet(value);
                setCurrentPage(0);
              }}>
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
                setCurrentPage(0);
              }}>
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="todos">Todos</SelectItem>
                  <SelectItem value="PENDIENTE">Pendiente</SelectItem>
                  <SelectItem value="CONFIRMADA">Confirmada</SelectItem>
                  <SelectItem value="ATENDIDA">Atendida</SelectItem>
                  <SelectItem value="CANCELADA">Cancelada</SelectItem>
                </SelectContent>
              </Select>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Calendario mejorado */}
      <AgendaCalendar
        citas={citasCalendario}
        fechaSeleccionada={selectedDate}
        onFechaChange={handleDateChange}
        onCitaClick={handleCitaClick}
        onCitaDrag={handleCitaDrag}
        veterinarioId={filtroVet}
        className="mb-6"
      />

      {/* Vista de disponibilidad de profesionales */}
      <DisponibilidadProfesionales
        citas={citasCalendario}
        veterinarios={veterinarios}
        fechaSeleccionada={selectedDate}
        className="mb-6"
      />

      {/* Lista de citas del día */}
      <Card>
          <CardHeader>
            <CardTitle className="flex items-center justify-between">
              <div className="flex items-center gap-2">
                <Clock className="h-5 w-5 text-primary" />
                Citas del Día - {format(selectedDate, 'PPP', { locale: es })} ({citasFiltradas.length})
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
            ) : citasFiltradas.length > 0 ? (
              <div className="space-y-3">
                {citasFiltradas.map((cita) => (
                  <div
                    key={cita.id}
                    className="flex items-center gap-4 p-4 rounded-lg border border-border hover:bg-accent/50 transition-colors"
                  >
                    <div 
                      className="flex flex-col items-center justify-center w-20 h-20 rounded-lg bg-primary/10 flex-shrink-0 cursor-pointer"
                      onClick={() => navigate(`/agenda/${cita.id}`)}
                    >
                      <span className="text-xs text-muted-foreground">Hora</span>
                      <span className="text-lg font-bold text-primary">
                        {new Date(cita.fecha).toLocaleTimeString('es-ES', { hour: '2-digit', minute: '2-digit' })}
                      </span>
                    </div>
                    <div 
                      className="flex-1 min-w-0 cursor-pointer"
                      onClick={() => navigate(`/agenda/${cita.id}`)}
                    >
                      <div className="flex items-center gap-2 mb-1">
                        <h4 className="font-semibold text-foreground">{cita.pacienteNombre || 'N/A'}</h4>
                      </div>
                      <p className="text-sm text-muted-foreground truncate">
                        Propietario: {cita.propietarioNombre || 'N/A'}
                      </p>
                      {cita.motivo && (
                        <p className="text-sm text-muted-foreground mt-1 truncate">{cita.motivo}</p>
                      )}
                    </div>
                    <Badge className={statusColors[cita.estado as keyof typeof statusColors]}>
                      {cita.estado.replace(/_/g, ' ')}
                    </Badge>
                    {/* Acciones rápidas */}
                    <DropdownMenu>
                      <DropdownMenuTrigger asChild>
                        <Button 
                          variant="ghost" 
                          size="icon"
                          disabled={updatingCitaId === cita.id}
                          onClick={(e) => e.stopPropagation()}
                        >
                          <MoreVertical className="h-4 w-4" />
                        </Button>
                      </DropdownMenuTrigger>
                      <DropdownMenuContent align="end">
                        <DropdownMenuItem onClick={(e) => {
                          e.stopPropagation();
                          navigate(`/agenda/${cita.id}`);
                        }}>
                          <Eye className="h-4 w-4 mr-2" />
                          Ver Detalle
                        </DropdownMenuItem>
                        {cita.estado === 'PENDIENTE' && (
                          <DropdownMenuItem 
                            onClick={(e) => {
                              e.stopPropagation();
                              handleUpdateEstado(cita.id, 'CONFIRMADA');
                            }}
                            disabled={updatingCitaId === cita.id}
                          >
                            <CheckCircle className="h-4 w-4 mr-2" />
                            Confirmar
                          </DropdownMenuItem>
                        )}
                        {cita.estado !== 'CANCELADA' && cita.estado !== 'ATENDIDA' && (
                          <DropdownMenuItem 
                            onClick={(e) => {
                              e.stopPropagation();
                              if (confirm('¿Estás seguro de cancelar esta cita?')) {
                                handleUpdateEstado(cita.id, 'CANCELADA');
                              }
                            }}
                            disabled={updatingCitaId === cita.id}
                            className="text-destructive"
                          >
                            <XCircle className="h-4 w-4 mr-2" />
                            Cancelar
                          </DropdownMenuItem>
                        )}
                      </DropdownMenuContent>
                    </DropdownMenu>
                  </div>
                ))}
              </div>
            ) : (
              <div className="text-center py-12">
                <Clock className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
                <h3 className="text-lg font-medium text-foreground">
                  {searchQuery ? 'No se encontraron citas' : 'No hay citas para esta fecha'}
                </h3>
                <p className="text-muted-foreground mt-1">
                  {searchQuery ? 'Intenta con otros términos de búsqueda' : 'Selecciona otra fecha o ajusta los filtros'}
                </p>
              </div>
            )}
          </CardContent>
      </Card>
    </div>
  );
}
