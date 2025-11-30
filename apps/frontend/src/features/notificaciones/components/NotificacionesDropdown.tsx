import { useState, useEffect } from 'react';
import { Bell, Check, CheckCheck, Trash2, Calendar, FileText, User, AlertCircle, Pill, Clock } from 'lucide-react';
import { Button } from '@shared/components/ui/button';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuTrigger,
  DropdownMenuSeparator,
} from '@shared/components/ui/dropdown-menu';
import { ScrollArea } from '@shared/components/ui/scroll-area';
import { Badge } from '@shared/components/ui/badge';
import { notificacionService, Notificacion } from '@features/notificaciones/services/notificacionService';
import { toast } from 'sonner';
import { formatDistanceToNow } from 'date-fns';
import { useLogger } from '@shared/hooks/useLogger';
import { useWebSocket } from '@shared/hooks/useWebSocket';

const tipoIcons = {
  CITA: Calendar,
  CONSULTA: FileText,
  PACIENTE: User,
  SISTEMA: AlertCircle,
  PRESCRIPCION: Pill,
  RECORDATORIO: Clock,
};

const tipoColors = {
  CITA: 'bg-blue-500/10 text-blue-500 border-blue-500/20',
  CONSULTA: 'bg-green-500/10 text-green-500 border-green-500/20',
  PACIENTE: 'bg-purple-500/10 text-purple-500 border-purple-500/20',
  SISTEMA: 'bg-orange-500/10 text-orange-500 border-orange-500/20',
  PRESCRIPCION: 'bg-pink-500/10 text-pink-500 border-pink-500/20',
  RECORDATORIO: 'bg-yellow-500/10 text-yellow-500 border-yellow-500/20',
};

export function NotificacionesDropdown() {
  const logger = useLogger('NotificacionesDropdown');
  const [notificaciones, setNotificaciones] = useState<Notificacion[]>([]);
  const [noLeidasCount, setNoLeidasCount] = useState(0);
  const [isLoading, setIsLoading] = useState(false);
  const [isOpen, setIsOpen] = useState(false);

  const loadNotificaciones = async () => {
    try {
      setIsLoading(true);
      const [all, count] = await Promise.all([
        notificacionService.getAll(),
        notificacionService.getCountNoLeidas(),
      ]);
      setNotificaciones(all);
      setNoLeidasCount(count);
    } catch (error: any) {
      logger.error('Error al cargar notificaciones', error, {
        action: 'loadNotificaciones',
      });
      toast.error('Error al cargar notificaciones');
    } finally {
      setIsLoading(false);
    }
  };

  // WebSocket para notificaciones en tiempo real
  useWebSocket({
    onNotification: (notification: any) => {
      // Normalizar notificación del WebSocket
      const normalizedNotification: Notificacion = {
        id: String(notification.id),
        usuarioId: String(notification.usuarioId),
        titulo: notification.titulo,
        mensaje: notification.mensaje,
        tipo: notification.tipo,
        leida: notification.leida || false,
        fechaCreacion: notification.fechaCreacion,
        entidadTipo: notification.entidadTipo,
        entidadId: notification.entidadId ? String(notification.entidadId) : undefined,
      };
      
      // Agregar nueva notificación al inicio de la lista (evitar duplicados)
      setNotificaciones((prev) => {
        const exists = prev.some((n) => n.id === normalizedNotification.id);
        if (exists) {
          return prev;
        }
        return [normalizedNotification, ...prev];
      });
      
      if (!normalizedNotification.leida) {
        setNoLeidasCount((prev) => prev + 1);
      }
      
      // Mostrar toast para notificaciones importantes
      if (normalizedNotification.tipo === 'CITA' || normalizedNotification.tipo === 'SISTEMA') {
        toast.info(normalizedNotification.titulo, {
          description: normalizedNotification.mensaje,
          duration: 5000,
        });
      }
    },
    onNotificationCount: (count: number) => {
      setNoLeidasCount(count);
    },
  });

  useEffect(() => {
    loadNotificaciones();
  }, []);

  useEffect(() => {
    if (isOpen) {
      loadNotificaciones();
    }
  }, [isOpen]);

  const handleMarcarComoLeida = async (id: string) => {
    try {
      await notificacionService.marcarComoLeida(id);
      await loadNotificaciones();
    } catch (error: any) {
      logger.warn('Error al marcar notificación como leída', {
        action: 'marcarComoLeida',
        notificacionId: id,
      });
      toast.error('Error al marcar como leída');
    }
  };

  const handleMarcarTodasComoLeidas = async () => {
    try {
      await notificacionService.marcarTodasComoLeidas();
      await loadNotificaciones();
      toast.success('Todas las notificaciones marcadas como leídas');
    } catch (error: any) {
      logger.warn('Error al marcar todas las notificaciones como leídas', {
        action: 'marcarTodasComoLeidas',
      });
      toast.error('Error al marcar todas como leídas');
    }
  };

  const handleEliminar = async (id: string) => {
    try {
      await notificacionService.delete(id);
      await loadNotificaciones();
      toast.success('Notificación eliminada');
    } catch (error: any) {
      logger.warn('Error al eliminar notificación', {
        action: 'eliminarNotificacion',
        notificacionId: id,
      });
      toast.error('Error al eliminar notificación');
    }
  };


  return (
    <DropdownMenu open={isOpen} onOpenChange={setIsOpen}>
      <DropdownMenuTrigger asChild>
        <Button variant="ghost" size="icon" className="relative">
          <Bell className="h-5 w-5" />
          {noLeidasCount > 0 && (
            <span className="absolute top-1 right-1 h-2 w-2 bg-destructive rounded-full" />
          )}
        </Button>
      </DropdownMenuTrigger>
      <DropdownMenuContent align="end" className="w-80 bg-popover">
        <div className="flex items-center justify-between p-4 border-b">
          <div className="flex items-center gap-2">
            <Bell className="h-4 w-4" />
            <h3 className="font-semibold">Notificaciones</h3>
            {noLeidasCount > 0 && (
              <Badge variant="destructive" className="ml-2">
                {noLeidasCount}
              </Badge>
            )}
          </div>
          {notificaciones.length > 0 && noLeidasCount > 0 && (
            <Button
              variant="ghost"
              size="sm"
              onClick={handleMarcarTodasComoLeidas}
              className="h-8 text-xs"
            >
              <CheckCheck className="h-3 w-3 mr-1" />
              Marcar todas
            </Button>
          )}
        </div>

        <ScrollArea className="h-[400px]">
          {isLoading ? (
            <div className="flex items-center justify-center p-8">
              <div className="animate-spin rounded-full h-6 w-6 border-b-2 border-primary"></div>
            </div>
          ) : notificaciones.length === 0 ? (
            <div className="flex flex-col items-center justify-center p-8 text-center">
              <Bell className="h-12 w-12 text-muted-foreground mb-2" />
              <p className="text-sm text-muted-foreground">No hay notificaciones</p>
            </div>
          ) : (
            <div className="divide-y">
              {notificaciones.map((notificacion) => {
                const Icon = tipoIcons[notificacion.tipo] || AlertCircle;
                const fecha = new Date(notificacion.fechaCreacion);
                const tiempoRelativo = formatDistanceToNow(fecha, { addSuffix: true });

                return (
                  <div
                    key={notificacion.id}
                    className={`p-4 hover:bg-accent transition-colors ${
                      !notificacion.leida ? 'bg-primary/5' : ''
                    }`}
                  >
                    <div className="flex items-start gap-3">
                      <div className={`p-2 rounded-full ${tipoColors[notificacion.tipo]}`}>
                        <Icon className="h-4 w-4" />
                      </div>
                      <div className="flex-1 min-w-0">
                        <div className="flex items-start justify-between gap-2">
                          <div className="flex-1">
                            <p className={`text-sm font-medium ${!notificacion.leida ? 'font-semibold' : ''}`}>
                              {notificacion.titulo}
                            </p>
                            <p className="text-xs text-muted-foreground mt-1 line-clamp-2">
                              {notificacion.mensaje}
                            </p>
                            <p className="text-xs text-muted-foreground mt-1">
                              {tiempoRelativo}
                            </p>
                          </div>
                          {!notificacion.leida && (
                            <div className="h-2 w-2 rounded-full bg-primary flex-shrink-0 mt-1" />
                          )}
                        </div>
                        <div className="flex items-center gap-2 mt-2">
                          <Button
                            variant="ghost"
                            size="sm"
                            className="h-7 text-xs"
                            onClick={() => handleMarcarComoLeida(notificacion.id)}
                          >
                            <Check className="h-3 w-3 mr-1" />
                            Leída
                          </Button>
                          <Button
                            variant="ghost"
                            size="sm"
                            className="h-7 text-xs text-destructive hover:text-destructive"
                            onClick={() => handleEliminar(notificacion.id)}
                          >
                            <Trash2 className="h-3 w-3" />
                          </Button>
                        </div>
                      </div>
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </ScrollArea>
      </DropdownMenuContent>
    </DropdownMenu>
  );
}

