import { Card, CardContent, CardHeader, CardTitle } from '@shared/components/ui/card';
import { Badge } from '@shared/components/ui/badge';
import { Separator } from '@shared/components/ui/separator';
import { User, Phone, Mail, Calendar, Clock } from 'lucide-react';
import { Paciente, Propietario } from '@core/types';
import { format } from 'date-fns';
import { es } from 'date-fns/locale';

interface InfoPacientePanelProps {
  paciente: Paciente;
  propietario?: Propietario;
  fechaCita?: Date;
}

export function InfoPacientePanel({ paciente, propietario, fechaCita }: InfoPacientePanelProps) {
  return (
    <Card className="sticky top-6">
      <CardHeader>
        <CardTitle className="flex items-center gap-2">
          <User className="h-5 w-5" />
          Información del Paciente
        </CardTitle>
      </CardHeader>
      <CardContent className="space-y-4">
        <div>
          <p className="text-sm text-muted-foreground">Nombre</p>
          <p className="font-semibold text-lg mt-1">{paciente.nombre}</p>
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

        {paciente.edadMeses && (
          <>
            <Separator />
            <div>
              <p className="text-sm text-muted-foreground">Edad</p>
              <p className="font-medium mt-1">
                {paciente.edadMeses < 12
                  ? `${paciente.edadMeses} meses`
                  : `${Math.floor(paciente.edadMeses / 12)} años ${paciente.edadMeses % 12} meses`}
              </p>
            </div>
          </>
        )}

        {paciente.pesoKg && (
          <>
            <Separator />
            <div>
              <p className="text-sm text-muted-foreground">Peso Actual</p>
              <p className="font-medium mt-1">{paciente.pesoKg} kg</p>
            </div>
          </>
        )}

        {fechaCita && (
          <>
            <Separator />
            <div>
              <p className="text-sm text-muted-foreground flex items-center gap-2">
                <Calendar className="h-4 w-4" />
                Fecha de Cita
              </p>
              <p className="font-medium mt-1">
                {format(fechaCita, "EEEE, d 'de' MMMM 'de' yyyy", { locale: es })}
              </p>
              <p className="text-sm text-muted-foreground mt-1 flex items-center gap-2">
                <Clock className="h-3 w-3" />
                {format(fechaCita, "HH:mm", { locale: es })} horas
              </p>
            </div>
          </>
        )}

        {propietario && (
          <>
            <Separator />
            <div>
              <p className="text-sm text-muted-foreground">Propietario</p>
              <p className="font-medium mt-1">{propietario.nombre}</p>
              {propietario.telefono && (
                <div className="flex items-center gap-2 mt-2">
                  <Phone className="h-3 w-3 text-muted-foreground" />
                  <a href={`tel:${propietario.telefono}`} className="text-sm text-primary hover:underline">
                    {propietario.telefono}
                  </a>
                </div>
              )}
              {propietario.email && (
                <div className="flex items-center gap-2 mt-1">
                  <Mail className="h-3 w-3 text-muted-foreground" />
                  <a href={`mailto:${propietario.email}`} className="text-sm text-primary hover:underline">
                    {propietario.email}
                  </a>
                </div>
              )}
            </div>
          </>
        )}
      </CardContent>
    </Card>
  );
}

