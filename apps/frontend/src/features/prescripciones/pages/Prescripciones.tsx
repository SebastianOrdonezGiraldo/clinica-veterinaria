import { useNavigate } from 'react-router-dom';
import { Pill, FileText, Calendar, User } from 'lucide-react';
import { Card, CardContent, CardHeader, CardTitle } from '@shared/components/ui/card';
import { Button } from '@shared/components/ui/button';
import { Badge } from '@shared/components/ui/badge';
import { mockPrescripciones, mockConsultas, getPacienteById, mockUsuarios } from '@shared/utils/mockData';

export default function Prescripciones() {
  const navigate = useNavigate();

  const prescripcionesConDetalles = mockPrescripciones.map(presc => {
    const consulta = mockConsultas.find(c => c.id === presc.consultaId);
    const paciente = consulta ? getPacienteById(consulta.pacienteId) : undefined;
    const profesional = consulta ? mockUsuarios.find(u => u.id === consulta.profesionalId) : undefined;
    
    return {
      ...presc,
      consulta,
      paciente,
      profesional,
    };
  });

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-3xl font-bold text-foreground">Prescripciones y Recetas</h1>
          <p className="text-muted-foreground mt-1">Gestión de prescripciones médicas</p>
        </div>
      </div>

      <div className="grid gap-4">
        {prescripcionesConDetalles.map((prescripcion) => (
          <Card key={prescripcion.id} className="hover:shadow-lg transition-shadow">
            <CardHeader>
              <div className="flex items-start justify-between">
                <div className="flex items-center gap-3">
                  <div className="h-12 w-12 rounded-full bg-info/10 flex items-center justify-center">
                    <Pill className="h-6 w-6 text-info" />
                  </div>
                  <div>
                    <CardTitle className="text-xl">Prescripción #{prescripcion.id}</CardTitle>
                    <div className="flex items-center gap-4 mt-2 text-sm text-muted-foreground">
                      {prescripcion.paciente && (
                        <div className="flex items-center gap-1">
                          <User className="h-4 w-4" />
                          {prescripcion.paciente.nombre}
                        </div>
                      )}
                      {prescripcion.consulta && (
                        <div className="flex items-center gap-1">
                          <Calendar className="h-4 w-4" />
                          {new Date(prescripcion.consulta.fecha).toLocaleDateString('es-ES')}
                        </div>
                      )}
                    </div>
                  </div>
                </div>
                <Button 
                  variant="outline"
                  onClick={() => navigate(`/prescripciones/${prescripcion.id}`)}
                  className="gap-2"
                >
                  <FileText className="h-4 w-4" />
                  Ver Receta
                </Button>
              </div>
            </CardHeader>
            <CardContent>
              <div className="space-y-3">
                <div>
                  <h4 className="font-semibold text-sm mb-2">Medicamentos Prescritos</h4>
                  <div className="space-y-2">
                    {prescripcion.items.map((item, index) => (
                      <div key={index} className="p-3 bg-accent/50 rounded-lg">
                        <div className="flex items-start justify-between mb-2">
                          <div>
                            <p className="font-medium">{item.medicamento}</p>
                            {item.presentacion && (
                              <p className="text-sm text-muted-foreground">{item.presentacion}</p>
                            )}
                          </div>
                          <Badge variant="outline" className="bg-info/10 text-info border-info/20">
                            {item.duracionDias} días
                          </Badge>
                        </div>
                        <div className="grid grid-cols-2 gap-2 text-sm">
                          <div>
                            <span className="text-muted-foreground">Dosis:</span>
                            <span className="ml-2 font-medium">{item.dosis}</span>
                          </div>
                          <div>
                            <span className="text-muted-foreground">Frecuencia:</span>
                            <span className="ml-2 font-medium">{item.frecuencia}</span>
                          </div>
                        </div>
                        {item.indicaciones && (
                          <p className="text-sm text-muted-foreground mt-2 italic">
                            {item.indicaciones}
                          </p>
                        )}
                      </div>
                    ))}
                  </div>
                </div>
                {prescripcion.profesional && (
                  <div className="pt-3 border-t">
                    <p className="text-sm text-muted-foreground">Prescrito por</p>
                    <p className="font-medium">{prescripcion.profesional.nombre}</p>
                  </div>
                )}
              </div>
            </CardContent>
          </Card>
        ))}
      </div>

      {prescripcionesConDetalles.length === 0 && (
        <Card>
          <CardContent className="text-center py-12">
            <Pill className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
            <h3 className="text-lg font-medium text-foreground">No hay prescripciones</h3>
            <p className="text-muted-foreground mt-1">Las prescripciones aparecerán aquí</p>
          </CardContent>
        </Card>
      )}
    </div>
  );
}
