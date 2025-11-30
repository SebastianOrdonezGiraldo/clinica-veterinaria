import { useState } from 'react';
import { Save, Bell, Clock, Calendar, AlertCircle } from 'lucide-react';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@shared/components/ui/card';
import { Button } from '@shared/components/ui/button';
import { Switch } from '@shared/components/ui/switch';
import { Label } from '@shared/components/ui/label';
import { Input } from '@shared/components/ui/input';
import { toast } from 'sonner';
import { useLogger } from '@shared/hooks/useLogger';

/**
 * Página de configuración de recordatorios automáticos.
 * 
 * Permite a los usuarios configurar qué tipos de recordatorios
 * desean recibir y con qué anticipación.
 */
export default function RecordatoriosConfig() {
  const logger = useLogger('RecordatoriosConfig');
  
  // Estado de configuración (por ahora solo en localStorage)
  const [config, setConfig] = useState({
    recordatoriosCitas: true,
    recordatoriosCitas24h: true,
    recordatoriosCitas1h: true,
    alertasVacunaciones: true,
    alertasStockBajo: true,
    sonidoNotificaciones: false,
  });

  const handleSave = () => {
    try {
      localStorage.setItem('recordatoriosConfig', JSON.stringify(config));
      toast.success('Configuración guardada exitosamente');
      logger.info('Configuración de recordatorios guardada', { config });
    } catch (error: any) {
      logger.error('Error al guardar configuración', error);
      toast.error('Error al guardar la configuración');
    }
  };

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold text-foreground">Configuración de Recordatorios</h1>
        <p className="text-muted-foreground mt-1">
          Personaliza cómo y cuándo recibes notificaciones
        </p>
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Calendar className="h-5 w-5 text-primary" />
            Recordatorios de Citas
          </CardTitle>
          <CardDescription>
            Recibe notificaciones antes de tus citas programadas
          </CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="flex items-center justify-between">
            <div className="space-y-0.5">
              <Label htmlFor="recordatorios-citas">Activar recordatorios de citas</Label>
              <p className="text-sm text-muted-foreground">
                Recibir notificaciones sobre tus citas programadas
              </p>
            </div>
            <Switch
              id="recordatorios-citas"
              checked={config.recordatoriosCitas}
              onCheckedChange={(checked) =>
                setConfig({ ...config, recordatoriosCitas: checked })
              }
            />
          </div>

          {config.recordatoriosCitas && (
            <div className="ml-6 space-y-4 border-l-2 border-border pl-6">
              <div className="flex items-center justify-between">
                <div className="space-y-0.5">
                  <Label htmlFor="recordatorios-24h" className="flex items-center gap-2">
                    <Clock className="h-4 w-4" />
                    Recordatorio 24 horas antes
                  </Label>
                  <p className="text-sm text-muted-foreground">
                    Te notificaremos un día antes de la cita
                  </p>
                </div>
                <Switch
                  id="recordatorios-24h"
                  checked={config.recordatoriosCitas24h}
                  onCheckedChange={(checked) =>
                    setConfig({ ...config, recordatoriosCitas24h: checked })
                  }
                />
              </div>

              <div className="flex items-center justify-between">
                <div className="space-y-0.5">
                  <Label htmlFor="recordatorios-1h" className="flex items-center gap-2">
                    <Clock className="h-4 w-4" />
                    Recordatorio 1 hora antes
                  </Label>
                  <p className="text-sm text-muted-foreground">
                    Te notificaremos una hora antes de la cita
                  </p>
                </div>
                <Switch
                  id="recordatorios-1h"
                  checked={config.recordatoriosCitas1h}
                  onCheckedChange={(checked) =>
                    setConfig({ ...config, recordatoriosCitas1h: checked })
                  }
                />
              </div>
            </div>
          )}
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <AlertCircle className="h-5 w-5 text-warning" />
            Alertas del Sistema
          </CardTitle>
          <CardDescription>
            Notificaciones importantes sobre el estado del sistema
          </CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="flex items-center justify-between">
            <div className="space-y-0.5">
              <Label htmlFor="alertas-vacunaciones">Alertas de vacunaciones</Label>
              <p className="text-sm text-muted-foreground">
                Recibir alertas sobre vacunaciones vencidas o próximas
              </p>
            </div>
            <Switch
              id="alertas-vacunaciones"
              checked={config.alertasVacunaciones}
              onCheckedChange={(checked) =>
                setConfig({ ...config, alertasVacunaciones: checked })
              }
            />
          </div>

          <div className="flex items-center justify-between">
            <div className="space-y-0.5">
              <Label htmlFor="alertas-stock">Alertas de stock bajo</Label>
              <p className="text-sm text-muted-foreground">
                Recibir notificaciones cuando productos tengan stock bajo
              </p>
            </div>
            <Switch
              id="alertas-stock"
              checked={config.alertasStockBajo}
              onCheckedChange={(checked) =>
                setConfig({ ...config, alertasStockBajo: checked })
              }
            />
          </div>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Bell className="h-5 w-5 text-secondary" />
            Preferencias de Notificaciones
          </CardTitle>
          <CardDescription>
            Personaliza cómo recibes las notificaciones
          </CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="flex items-center justify-between">
            <div className="space-y-0.5">
              <Label htmlFor="sonido-notificaciones">Sonido de notificaciones</Label>
              <p className="text-sm text-muted-foreground">
                Reproducir sonido cuando llegue una notificación importante
              </p>
            </div>
            <Switch
              id="sonido-notificaciones"
              checked={config.sonidoNotificaciones}
              onCheckedChange={(checked) =>
                setConfig({ ...config, sonidoNotificaciones: checked })
              }
            />
          </div>
        </CardContent>
      </Card>

      <div className="flex justify-end gap-4">
        <Button variant="outline" onClick={() => window.history.back()}>
          Cancelar
        </Button>
        <Button onClick={handleSave} className="gap-2">
          <Save className="h-4 w-4" />
          Guardar Configuración
        </Button>
      </div>
    </div>
  );
}

