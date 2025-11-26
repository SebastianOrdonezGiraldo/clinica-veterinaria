import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { ArrowLeft, Edit, Calendar, Clock, User, Phone, Mail, MapPin, CheckCircle, XCircle, AlertCircle, Loader2, FileText } from 'lucide-react';
import { Button } from '@shared/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@shared/components/ui/card';
import { Badge } from '@shared/components/ui/badge';
import { Separator } from '@shared/components/ui/separator';
import { AlertDialog, AlertDialogAction, AlertDialogCancel, AlertDialogContent, AlertDialogDescription, AlertDialogFooter, AlertDialogHeader, AlertDialogTitle } from '@shared/components/ui/alert-dialog';
import { citaService } from '@features/agenda/services/citaService';
import { pacienteService } from '@features/pacientes/services/pacienteService';
import { propietarioService } from '@features/propietarios/services/propietarioService';
import { usuarioService } from '@features/usuarios/services/usuarioService';
import { useAuth } from '@core/auth/AuthContext';
import { Cita, Paciente, Propietario, Usuario } from '@core/types';
import { toast } from 'sonner';
import { format } from 'date-fns';
import { es } from 'date-fns/locale';

const statusColors = {
  CONFIRMADA: 'bg-status-confirmed/10 text-status-confirmed border-status-confirmed/20',
  PENDIENTE: 'bg-status-pending/10 text-status-pending border-status-pending/20',
  CANCELADA: 'bg-status-cancelled/10 text-status-cancelled border-status-cancelled/20',
  ATENDIDA: 'bg-status-completed/10 text-status-completed border-status-completed/20',
};

const statusLabels = {
  CONFIRMADA: 'Confirmada',
  PENDIENTE: 'Pendiente',
  CANCELADA: 'Cancelada',
  ATENDIDA: 'Atendida',
};

export default function CitaDetalle() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { user } = useAuth();
  const [cita, setCita] = useState<Cita | null>(null);
  const [paciente, setPaciente] = useState<Paciente | null>(null);
  const [propietario, setPropietario] = useState<Propietario | null>(null);
  const [profesional, setProfesional] = useState<Usuario | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isUpdating, setIsUpdating] = useState(false);
  const [showConfirmDialog, setShowConfirmDialog] = useState(false);
  const [showCancelDialog, setShowCancelDialog] = useState(false);
  const [showAttendDialog, setShowAttendDialog] = useState(false);

  useEffect(() => {
    if (id) {
      loadCita();
    }
  }, [id]);

  const loadCita = async () => {
    if (!id) return;
    
    try {
      setIsLoading(true);
      const citaData = await citaService.getById(id);
      setCita(citaData);

      // Cargar datos relacionados
      const [pacienteData, propietarioData, profesionalData] = await Promise.allSettled([
        pacienteService.getById(citaData.pacienteId),
        propietarioService.getById(citaData.propietarioId),
        usuarioService.getById(citaData.profesionalId),
      ]);

      if (pacienteData.status === 'fulfilled') {
        setPaciente(pacienteData.value);
      }

      if (propietarioData.status === 'fulfilled') {
        setPropietario(propietarioData.value);
      }

      if (profesionalData.status === 'fulfilled') {
        setProfesional(profesionalData.value);
      }
    } catch (error: any) {
      console.error('Error al cargar cita:', error);
      const errorMessage = error?.response?.data?.message || 'Error al cargar la cita';
      toast.error(errorMessage);
      navigate('/agenda');
    } finally {
      setIsLoading(false);
    }
  };

  const handleUpdateEstado = async (nuevoEstado: 'CONFIRMADA' | 'CANCELADA' | 'ATENDIDA') => {
    if (!cita) return;

    try {
      setIsUpdating(true);
      const citaActualizada = await citaService.updateEstado(cita.id, nuevoEstado);
      setCita(citaActualizada);
      toast.success(`Cita ${statusLabels[nuevoEstado].toLowerCase()} exitosamente`);
      
      // Cerrar diálogos
      setShowConfirmDialog(false);
      setShowCancelDialog(false);
      setShowAttendDialog(false);
    } catch (error: any) {
      console.error('Error al actualizar estado:', error);
      const errorMessage = error?.response?.data?.message || 'Error al actualizar el estado de la cita';
      toast.error(errorMessage);
    } finally {
      setIsUpdating(false);
    }
  };

  if (isLoading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="text-center">
          <Loader2 className="h-8 w-8 animate-spin mx-auto mb-4 text-primary" />
          <p className="text-muted-foreground">Cargando información de la cita...</p>
        </div>
      </div>
    );
  }

  if (!cita) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="text-center">
          <AlertCircle className="h-12 w-12 text-destructive mx-auto mb-4" />
          <h1 className="text-2xl font-bold text-foreground mb-2">Cita no encontrada</h1>
          <p className="text-muted-foreground mb-4">La cita que buscas no existe o fue eliminada</p>
          <Button onClick={() => navigate('/agenda')}>Volver a Agenda</Button>
        </div>
      </div>
    );
  }

  const fechaCita = new Date(cita.fecha);
  const puedeConfirmar = cita.estado === 'PENDIENTE';
  const puedeCancelar = cita.estado !== 'CANCELADA' && cita.estado !== 'ATENDIDA';
  const puedeAtender = cita.estado === 'CONFIRMADA' || cita.estado === 'PENDIENTE';
  const puedeIniciarConsulta = (user?.rol === 'VET' || user?.rol === 'ADMIN') && 
                                cita.estado !== 'CANCELADA' && 
                                cita.estado !== 'ATENDIDA';

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-4">
          <Button variant="ghost" size="icon" onClick={() => navigate('/agenda')}>
            <ArrowLeft className="h-4 w-4" />
          </Button>
          <div>
            <h1 className="text-3xl font-bold text-foreground">Detalle de Cita</h1>
            <p className="text-muted-foreground mt-1">
              {format(fechaCita, "EEEE, d 'de' MMMM 'de' yyyy 'a las' HH:mm", { locale: es })}
            </p>
          </div>
        </div>
        <div className="flex items-center gap-2">
          <Badge className={statusColors[cita.estado as keyof typeof statusColors]}>
            {statusLabels[cita.estado as keyof typeof statusLabels]}
          </Badge>
          <Button variant="outline" onClick={() => navigate(`/agenda/${cita.id}/editar`)}>
            <Edit className="h-4 w-4 mr-2" />
            Editar
          </Button>
        </div>
      </div>

      <div className="grid gap-6 md:grid-cols-2">
        {/* Información de la Cita */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Calendar className="h-5 w-5" />
              Información de la Cita
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div>
              <p className="text-sm text-muted-foreground">Fecha y Hora</p>
              <div className="flex items-center gap-2 mt-1">
                <Clock className="h-4 w-4 text-muted-foreground" />
                <p className="font-medium">
                  {format(fechaCita, "EEEE, d 'de' MMMM 'de' yyyy", { locale: es })}
                </p>
              </div>
              <p className="font-medium ml-6">
                {format(fechaCita, "HH:mm", { locale: es })} horas
              </p>
            </div>

            <Separator />

            <div>
              <p className="text-sm text-muted-foreground">Motivo de Consulta</p>
              <p className="font-medium mt-1">{cita.motivo || 'No especificado'}</p>
            </div>

            {cita.observaciones && (
              <>
                <Separator />
                <div>
                  <p className="text-sm text-muted-foreground">Observaciones</p>
                  <p className="font-medium mt-1">{cita.observaciones}</p>
                </div>
              </>
            )}

            <Separator />

            <div>
              <p className="text-sm text-muted-foreground">Veterinario Asignado</p>
              <div className="flex items-center gap-2 mt-1">
                <User className="h-4 w-4 text-muted-foreground" />
                <p className="font-medium">{profesional?.nombre || 'N/A'}</p>
              </div>
            </div>
          </CardContent>
        </Card>

        {/* Información del Paciente */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <User className="h-5 w-5" />
              Información del Paciente
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            {paciente ? (
              <>
                <div>
                  <p className="text-sm text-muted-foreground">Nombre</p>
                  <p className="font-medium mt-1">{paciente.nombre}</p>
                </div>

                <Separator />

                <div>
                  <p className="text-sm text-muted-foreground">Especie</p>
                  <Badge variant="outline" className="mt-1">
                    {paciente.especie}
                  </Badge>
                </div>

                {paciente.raza && (
                  <>
                    <Separator />
                    <div>
                      <p className="text-sm text-muted-foreground">Raza</p>
                      <p className="font-medium mt-1">{paciente.raza}</p>
                    </div>
                  </>
                )}

                <Separator />

                <div>
                  <p className="text-sm text-muted-foreground">Propietario</p>
                  <p className="font-medium mt-1">{propietario?.nombre || 'N/A'}</p>
                  {propietario?.telefono && (
                    <div className="flex items-center gap-2 mt-1">
                      <Phone className="h-3 w-3 text-muted-foreground" />
                      <a href={`tel:${propietario.telefono}`} className="text-sm text-primary hover:underline">
                        {propietario.telefono}
                      </a>
                    </div>
                  )}
                  {propietario?.email && (
                    <div className="flex items-center gap-2 mt-1">
                      <Mail className="h-3 w-3 text-muted-foreground" />
                      <a href={`mailto:${propietario.email}`} className="text-sm text-primary hover:underline">
                        {propietario.email}
                      </a>
                    </div>
                  )}
                </div>

                <Separator />

                <Button
                  variant="outline"
                  className="w-full"
                  onClick={() => navigate(`/pacientes/${paciente.id}`)}
                >
                  Ver Perfil Completo del Paciente
                </Button>
              </>
            ) : (
              <p className="text-muted-foreground">No se pudo cargar la información del paciente</p>
            )}
          </CardContent>
        </Card>
      </div>

      {/* Acciones */}
      <Card>
        <CardHeader>
          <CardTitle>Acciones</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="flex flex-wrap gap-3">
            {puedeIniciarConsulta && (
              <Button
                onClick={() => navigate(`/agenda/${cita.id}/consulta`)}
                disabled={isUpdating}
                className="gap-2"
              >
                <FileText className="h-4 w-4" />
                Iniciar Consulta
              </Button>
            )}

            {puedeConfirmar && (
              <Button
                onClick={() => setShowConfirmDialog(true)}
                disabled={isUpdating}
                className="gap-2"
              >
                <CheckCircle className="h-4 w-4" />
                Confirmar Cita
              </Button>
            )}

            {puedeAtender && (
              <Button
                variant="default"
                onClick={() => setShowAttendDialog(true)}
                disabled={isUpdating}
                className="gap-2"
              >
                <CheckCircle className="h-4 w-4" />
                Marcar como Atendida
              </Button>
            )}

            {puedeCancelar && (
              <Button
                variant="destructive"
                onClick={() => setShowCancelDialog(true)}
                disabled={isUpdating}
                className="gap-2"
              >
                <XCircle className="h-4 w-4" />
                Cancelar Cita
              </Button>
            )}
          </div>
        </CardContent>
      </Card>

      {/* Diálogo de Confirmación */}
      <AlertDialog open={showConfirmDialog} onOpenChange={setShowConfirmDialog}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Confirmar Cita</AlertDialogTitle>
            <AlertDialogDescription>
              ¿Estás seguro de que deseas confirmar esta cita? El estado cambiará a "Confirmada".
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel disabled={isUpdating}>Cancelar</AlertDialogCancel>
            <AlertDialogAction
              onClick={() => handleUpdateEstado('CONFIRMADA')}
              disabled={isUpdating}
            >
              {isUpdating ? (
                <>
                  <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                  Confirmando...
                </>
              ) : (
                'Confirmar'
              )}
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>

      {/* Diálogo de Cancelación */}
      <AlertDialog open={showCancelDialog} onOpenChange={setShowCancelDialog}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Cancelar Cita</AlertDialogTitle>
            <AlertDialogDescription>
              ¿Estás seguro de que deseas cancelar esta cita? Esta acción no se puede deshacer.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel disabled={isUpdating}>No Cancelar</AlertDialogCancel>
            <AlertDialogAction
              onClick={() => handleUpdateEstado('CANCELADA')}
              disabled={isUpdating}
              className="bg-destructive text-destructive-foreground hover:bg-destructive/90"
            >
              {isUpdating ? (
                <>
                  <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                  Cancelando...
                </>
              ) : (
                'Sí, Cancelar'
              )}
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>

      {/* Diálogo de Atender */}
      <AlertDialog open={showAttendDialog} onOpenChange={setShowAttendDialog}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Marcar como Atendida</AlertDialogTitle>
            <AlertDialogDescription>
              ¿Estás seguro de que deseas marcar esta cita como atendida? Esto cambiará el estado a "Atendida".
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel disabled={isUpdating}>Cancelar</AlertDialogCancel>
            <AlertDialogAction
              onClick={() => handleUpdateEstado('ATENDIDA')}
              disabled={isUpdating}
            >
              {isUpdating ? (
                <>
                  <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                  Actualizando...
                </>
              ) : (
                'Marcar como Atendida'
              )}
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  );
}

