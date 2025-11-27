import { useState, useEffect, useCallback } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@shared/components/ui/card';
import { Button } from '@shared/components/ui/button';
import { Skeleton } from '@shared/components/ui/skeleton';
import { FileText, Calendar, ChevronRight, Loader2 } from 'lucide-react';
import { consultaService } from '@features/historias/services/consultaService';
import { Consulta } from '@core/types';
import { format } from 'date-fns';
import { es } from 'date-fns/locale';
import { useNavigate } from 'react-router-dom';
import { useLogger } from '@shared/hooks/useLogger';

interface HistorialRapidoProps {
  pacienteId: string;
  limit?: number;
}

export function HistorialRapido({ pacienteId, limit = 5 }: HistorialRapidoProps) {
  const logger = useLogger('HistorialRapido');
  const navigate = useNavigate();
  const [consultas, setConsultas] = useState<Consulta[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  const loadHistorial = useCallback(async () => {
    try {
      setIsLoading(true);
      const result = await consultaService.searchWithFilters({
        pacienteId,
        page: 0,
        size: limit,
        sort: 'fecha,desc',
      });
      setConsultas(result.content);
    } catch (error) {
      logger.warn('Error al cargar historial rápido del paciente', {
        action: 'loadHistorial',
        pacienteId,
        limit,
      });
    } finally {
      setIsLoading(false);
    }
  }, [pacienteId, limit]);

  useEffect(() => {
    loadHistorial();
  }, [loadHistorial]);

  if (isLoading) {
    return (
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <FileText className="h-5 w-5" />
            Historial Clínico
          </CardTitle>
        </CardHeader>
        <CardContent className="space-y-3">
          {[...Array(limit)].map((_, i) => (
            <Skeleton key={i} className="h-16 w-full" />
          ))}
        </CardContent>
      </Card>
    );
  }

  if (consultas.length === 0) {
    return (
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <FileText className="h-5 w-5" />
            Historial Clínico
          </CardTitle>
        </CardHeader>
        <CardContent>
          <p className="text-sm text-muted-foreground text-center py-4">
            No hay consultas previas registradas
          </p>
        </CardContent>
      </Card>
    );
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle className="flex items-center gap-2">
          <FileText className="h-5 w-5" />
          Historial Clínico
        </CardTitle>
      </CardHeader>
      <CardContent className="space-y-3">
        {consultas.map((consulta) => (
          <div
            key={consulta.id}
            className="p-3 rounded-lg border border-border hover:bg-accent/50 transition-colors cursor-pointer"
            onClick={() => navigate(`/historias/${consulta.id}`)}
          >
            <div className="flex items-start justify-between gap-2">
              <div className="flex-1 min-w-0">
                <div className="flex items-center gap-2 mb-1">
                  <Calendar className="h-3 w-3 text-muted-foreground flex-shrink-0" />
                  <span className="text-sm font-medium">
                    {format(new Date(consulta.fecha), "d 'de' MMM 'de' yyyy", { locale: es })}
                  </span>
                </div>
                {consulta.diagnostico && (
                  <p className="text-sm text-muted-foreground truncate">{consulta.diagnostico}</p>
                )}
                {consulta.profesionalNombre && (
                  <p className="text-xs text-muted-foreground mt-1">
                    Por: {consulta.profesionalNombre}
                  </p>
                )}
              </div>
              <ChevronRight className="h-4 w-4 text-muted-foreground flex-shrink-0" />
            </div>
          </div>
        ))}
        <Button
          variant="outline"
          className="w-full mt-2"
          onClick={() => navigate(`/historias/${pacienteId}`)}
        >
          Ver Historial Completo
        </Button>
      </CardContent>
    </Card>
  );
}

