import { useParams, useNavigate } from 'react-router-dom';
import { ArrowLeft, Download, Printer, Pill } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Separator } from '@/components/ui/separator';
import { mockPrescripciones, mockConsultas, getPacienteById, mockUsuarios } from '@/lib/mockData';
import { toast } from 'sonner';

export default function PrescripcionDetalle() {
  const { id } = useParams();
  const navigate = useNavigate();
  
  const prescripcion = mockPrescripciones.find(p => p.id === id);
  const consulta = prescripcion ? mockConsultas.find(c => c.id === prescripcion.consultaId) : undefined;
  const paciente = consulta ? getPacienteById(consulta.pacienteId) : undefined;
  const profesional = consulta ? mockUsuarios.find(u => u.id === consulta.profesionalId) : undefined;

  if (!prescripcion || !consulta || !paciente) {
    return (
      <div className="text-center py-12">
        <h2 className="text-2xl font-bold">Prescripción no encontrada</h2>
      </div>
    );
  }

  const handleGeneratePDF = () => {
    toast.success('PDF generado exitosamente (simulado)');
  };

  const handlePrint = () => {
    window.print();
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center gap-4">
        <Button variant="ghost" size="icon" onClick={() => navigate('/prescripciones')}>
          <ArrowLeft className="h-5 w-5" />
        </Button>
        <div className="flex-1">
          <h1 className="text-3xl font-bold text-foreground">Receta Médica</h1>
          <p className="text-muted-foreground mt-1">Prescripción #{prescripcion.id}</p>
        </div>
        <div className="flex gap-2">
          <Button variant="outline" onClick={handlePrint} className="gap-2">
            <Printer className="h-4 w-4" />
            Imprimir
          </Button>
          <Button onClick={handleGeneratePDF} className="gap-2">
            <Download className="h-4 w-4" />
            Descargar PDF
          </Button>
        </div>
      </div>

      <Card className="print:shadow-none">
        <CardHeader className="bg-primary/5">
          <div className="flex items-center justify-between">
            <div>
              <CardTitle className="text-2xl">🐾 VetClinic Pro</CardTitle>
              <p className="text-sm text-muted-foreground mt-1">Sistema de Gestión Veterinaria</p>
            </div>
            <div className="h-16 w-16 rounded-full bg-primary/10 flex items-center justify-center">
              <Pill className="h-8 w-8 text-primary" />
            </div>
          </div>
        </CardHeader>
        <CardContent className="space-y-6 pt-6">
          <div className="grid md:grid-cols-2 gap-6">
            <div>
              <h3 className="font-semibold text-sm text-muted-foreground mb-2">Información del Paciente</h3>
              <div className="space-y-1">
                <p className="font-medium text-lg">{paciente.nombre}</p>
                <p className="text-sm">Especie: {paciente.especie}</p>
                <p className="text-sm">Raza: {paciente.raza || 'N/A'}</p>
                <p className="text-sm">Propietario: {paciente.propietario?.nombre}</p>
              </div>
            </div>
            <div>
              <h3 className="font-semibold text-sm text-muted-foreground mb-2">Información de la Consulta</h3>
              <div className="space-y-1">
                <p className="text-sm">Fecha: {new Date(consulta.fecha).toLocaleDateString('es-ES', { 
                  day: '2-digit', 
                  month: 'long', 
                  year: 'numeric' 
                })}</p>
                <p className="text-sm">Profesional: {profesional?.nombre}</p>
              </div>
            </div>
          </div>

          <Separator />

          <div>
            <h3 className="font-semibold text-lg mb-4 flex items-center gap-2">
              <Pill className="h-5 w-5 text-primary" />
              Medicamentos Prescritos
            </h3>
            <div className="space-y-4">
              {prescripcion.items.map((item, index) => (
                <div key={index} className="p-4 border rounded-lg bg-accent/30">
                  <div className="flex items-start justify-between mb-3">
                    <div>
                      <h4 className="font-semibold text-lg">{item.medicamento}</h4>
                      {item.presentacion && (
                        <p className="text-sm text-muted-foreground">{item.presentacion}</p>
                      )}
                    </div>
                    <div className="text-right">
                      <p className="text-sm font-medium text-primary">Duración</p>
                      <p className="text-lg font-bold">{item.duracionDias} días</p>
                    </div>
                  </div>
                  
                  <div className="grid md:grid-cols-2 gap-3 mb-3">
                    <div className="flex items-center gap-2">
                      <span className="text-sm font-medium text-muted-foreground">Dosis:</span>
                      <span className="font-medium">{item.dosis}</span>
                    </div>
                    <div className="flex items-center gap-2">
                      <span className="text-sm font-medium text-muted-foreground">Frecuencia:</span>
                      <span className="font-medium">{item.frecuencia}</span>
                    </div>
                  </div>

                  {item.indicaciones && (
                    <div className="pt-3 border-t">
                      <p className="text-sm font-medium text-muted-foreground mb-1">Indicaciones:</p>
                      <p className="text-sm">{item.indicaciones}</p>
                    </div>
                  )}
                </div>
              ))}
            </div>
          </div>

          <Separator />

          <div className="text-center text-sm text-muted-foreground">
            <p>Esta receta es válida por 30 días desde la fecha de emisión</p>
            <p className="mt-2">VetClinic Pro • Sistema de Gestión Veterinaria</p>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
