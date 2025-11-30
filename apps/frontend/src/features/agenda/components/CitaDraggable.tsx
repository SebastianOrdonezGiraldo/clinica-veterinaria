import { useSortable } from '@dnd-kit/sortable';
import { CSS } from '@dnd-kit/utilities';
import { Clock } from 'lucide-react';
import { Badge } from '@shared/components/ui/badge';
import { Cita } from '@core/types';
import { cn } from '@shared/utils/utils';
import { format } from 'date-fns';

const statusColors = {
  CONFIRMADA: 'bg-status-confirmed/10 text-status-confirmed border-status-confirmed/20',
  PENDIENTE: 'bg-status-pending/10 text-status-pending border-status-pending/20',
  EN_PROCESO: 'bg-blue-500/10 text-blue-500 border-blue-500/20',
  COMPLETADA: 'bg-status-completed/10 text-status-completed border-status-completed/20',
  CANCELADA: 'bg-status-cancelled/10 text-status-cancelled border-status-cancelled/20',
  ATENDIDA: 'bg-status-completed/10 text-status-completed border-status-completed/20',
};

interface CitaDraggableProps {
  cita: Cita;
  onClick?: () => void;
  className?: string;
  compact?: boolean;
}

export default function CitaDraggable({ cita, onClick, className, compact = false }: CitaDraggableProps) {
  const {
    attributes,
    listeners,
    setNodeRef,
    transform,
    transition,
    isDragging,
  } = useSortable({
    id: cita.id,
    disabled: cita.estado === 'ATENDIDA' || cita.estado === 'CANCELADA', // No arrastrar citas completadas o canceladas
  });

  const style = {
    transform: CSS.Transform.toString(transform),
    transition,
    opacity: isDragging ? 0.5 : 1,
  };

  if (compact) {
    return (
      <div
        ref={setNodeRef}
        style={style}
        {...attributes}
        {...listeners}
        className={cn(
          "text-xs p-1 rounded truncate cursor-move",
          statusColors[cita.estado as keyof typeof statusColors] || "bg-gray-100",
          isDragging && "z-50",
          className
        )}
        onClick={onClick}
      >
        {format(new Date(cita.fecha), 'HH:mm')} - {cita.pacienteNombre}
      </div>
    );
  }

  return (
    <div
      ref={setNodeRef}
      style={style}
      {...attributes}
      {...listeners}
      className={cn(
        "p-3 rounded-lg border cursor-move hover:shadow-md transition-shadow",
        statusColors[cita.estado as keyof typeof statusColors] || "bg-gray-100",
        isDragging && "z-50 shadow-lg",
        className
      )}
      onClick={onClick}
    >
      <div className="flex items-center justify-between mb-1">
        <div className="flex items-center gap-2">
          <Clock className="h-4 w-4 text-muted-foreground" />
          <span className="font-semibold text-sm">
            {format(new Date(cita.fecha), 'HH:mm')}
          </span>
        </div>
        <Badge className={statusColors[cita.estado as keyof typeof statusColors]}>
          {cita.estado}
        </Badge>
      </div>
      <div className="text-sm font-medium">{cita.pacienteNombre}</div>
      <div className="text-xs text-muted-foreground">{cita.propietarioNombre}</div>
      {cita.motivo && (
        <div className="text-xs text-muted-foreground mt-1">{cita.motivo}</div>
      )}
    </div>
  );
}

