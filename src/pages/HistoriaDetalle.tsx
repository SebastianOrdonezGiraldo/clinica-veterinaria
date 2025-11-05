import { useParams, useNavigate } from 'react-router-dom';
import { ArrowLeft, Plus, Calendar, User, Activity, FileText, Pill } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Separator } from '@/components/ui/separator';
import { getPacienteById, getConsultasByPaciente, mockPrescripciones } from '@/lib/mockData';

export default function HistoriaDetalle() {
  const { id } = useParams();
  const navigate = useNavigate();
  const paciente = id ? getPacienteById(id) : undefined;
  const consultas = id ? getConsultasByPaciente(id) : [];

  if (!paciente) {
    return (
      <div className="text-center py-12">
        <h2 className="text-2xl font-bold">Paciente no encontrado</h2>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center gap-4">
        <Button variant="ghost" size="icon" onClick={() => navigate('/historias')}>
          <ArrowLeft className="h-5 w-5" />
        </Button>
        <div className="flex-1">
          <h1 className="text-3xl font-bold text-foreground">Historia Clínica - {paciente.nombre}</h1>
          <p className="text-muted-foreground mt-1">{paciente.especie} • {paciente.raza}</p>
        </div>
        <Button className="gap-2">
          <Plus className="h-4 w-4" />
          Nueva Consulta
        </Button>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Información del Paciente</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
            <div>
              <p className="text-sm text-muted-foreground">Propietario</p>
              <p className="font-medium">{paciente.propietario?.nombre}</p>
            </div>
            <div>
              <p className="text-sm text-muted-foreground">Sexo</p>
              <p className="font-medium">{paciente.sexo === 'M' ? 'Macho' : 'Hembra'}</p>
            </div>
            <div>
              <p className="text-sm text-muted-foreground">Edad</p>
              <p className="font-medium">
                {paciente.edadMeses ? `${Math.floor(paciente.edadMeses / 12)}a ${paciente.edadMeses % 12}m` : 'N/A'}
              </p>
            </div>
            <div>
              <p className="text-sm text-muted-foreground">Peso Actual</p>
              <p className="font-medium">{paciente.pesoKg ? `${paciente.pesoKg} kg` : 'N/A'}</p>
            </div>
          </div>
          {paciente.microchip && (
            <div className="mt-4 pt-4 border-t">
              <p className="text-sm text-muted-foreground">Microchip</p>
              <p className="font-medium">{paciente.microchip}</p>
            </div>
          )}
        </CardContent>
      </Card>

      <div>
        <h2 className="text-xl font-semibold mb-4">Timeline de Consultas</h2>
        <div className="space-y-4">
          {consultas.map((consulta, index) => {
            const prescripcion = mockPrescripciones.find(p => p.consultaId === consulta.id);
            
            return (
              <Card key={consulta.id} className="relative">
                {index !== consultas.length - 1 && (
                  <div className="absolute left-6 top-16 bottom-0 w-0.5 bg-border -mb-4" />
                )}
                <CardHeader>
                  <div className="flex items-start justify-between">
                    <div className="flex items-center gap-3">
                      <div className="h-10 w-10 rounded-full bg-primary/10 flex items-center justify-center relative z-10">
                        <Activity className="h-5 w-5 text-primary" />
                      </div>
                      <div>
                        <CardTitle className="text-lg">
                          Consulta - {new Date(consulta.fecha).toLocaleDateString('es-ES', { 
                            day: '2-digit', 
                            month: 'long', 
                            year: 'numeric' 
                          })}
                        </CardTitle>
                        <div className="flex items-center gap-2 mt-1">
                          <User className="h-4 w-4 text-muted-foreground" />
                          <p className="text-sm text-muted-foreground">{consulta.profesional?.nombre}</p>
                        </div>
                      </div>
                    </div>
                    {prescripcion && (
                      <Badge className="bg-info/10 text-info border-info/20">
                        <Pill className="h-3 w-3 mr-1" />
                        Con Prescripción
                      </Badge>
                    )}
                  </div>
                </CardHeader>
                <CardContent className="space-y-4">
                  {consulta.signosVitales && (
                    <div>
                      <h4 className="font-semibold text-sm mb-2 flex items-center gap-2">
                        <Activity className="h-4 w-4 text-primary" />
                        Signos Vitales
                      </h4>
                      <div className="grid grid-cols-2 md:grid-cols-4 gap-3 p-3 bg-accent/50 rounded-lg">
                        {consulta.signosVitales.fc && (
                          <div>
                            <p className="text-xs text-muted-foreground">FC</p>
                            <p className="text-sm font-medium">{consulta.signosVitales.fc} lpm</p>
                          </div>
                        )}
                        {consulta.signosVitales.fr && (
                          <div>
                            <p className="text-xs text-muted-foreground">FR</p>
                            <p className="text-sm font-medium">{consulta.signosVitales.fr} rpm</p>
                          </div>
                        )}
                        {consulta.signosVitales.temp && (
                          <div>
                            <p className="text-xs text-muted-foreground">Temperatura</p>
                            <p className="text-sm font-medium">{consulta.signosVitales.temp}°C</p>
                          </div>
                        )}
                        {consulta.signosVitales.peso && (
                          <div>
                            <p className="text-xs text-muted-foreground">Peso</p>
                            <p className="text-sm font-medium">{consulta.signosVitales.peso} kg</p>
                          </div>
                        )}
                      </div>
                    </div>
                  )}

                  {consulta.examen && (
                    <div>
                      <h4 className="font-semibold text-sm mb-2">Examen Físico</h4>
                      <p className="text-sm text-muted-foreground">{consulta.examen}</p>
                    </div>
                  )}

                  {consulta.diagnosticos && consulta.diagnosticos.length > 0 && (
                    <div>
                      <h4 className="font-semibold text-sm mb-2">Diagnósticos</h4>
                      <div className="flex flex-wrap gap-2">
                        {consulta.diagnosticos.map((diag, i) => (
                          <Badge key={i} variant="outline" className="bg-secondary/10 text-secondary border-secondary/20">
                            {diag}
                          </Badge>
                        ))}
                      </div>
                    </div>
                  )}

                  {consulta.procedimientos && consulta.procedimientos.length > 0 && (
                    <div>
                      <h4 className="font-semibold text-sm mb-2">Procedimientos</h4>
                      <ul className="space-y-1">
                        {consulta.procedimientos.map((proc, i) => (
                          <li key={i} className="text-sm text-muted-foreground flex items-center gap-2">
                            <span className="h-1.5 w-1.5 rounded-full bg-primary" />
                            {proc}
                          </li>
                        ))}
                      </ul>
                    </div>
                  )}

                  {prescripcion && (
                    <div className="pt-3 border-t">
                      <Button 
                        variant="outline" 
                        size="sm"
                        onClick={() => navigate(`/prescripciones/${prescripcion.id}`)}
                        className="gap-2"
                      >
                        <FileText className="h-4 w-4" />
                        Ver Prescripción
                      </Button>
                    </div>
                  )}
                </CardContent>
              </Card>
            );
          })}

          {consultas.length === 0 && (
            <Card>
              <CardContent className="text-center py-12">
                <FileText className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
                <h3 className="text-lg font-medium text-foreground">Sin consultas registradas</h3>
                <p className="text-muted-foreground mt-1">Este paciente aún no tiene consultas</p>
              </CardContent>
            </Card>
          )}
        </div>
      </div>
    </div>
  );
}
