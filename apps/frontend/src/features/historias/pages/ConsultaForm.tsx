import { useNavigate, useParams } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';
import { ArrowLeft, Save, Plus, X } from 'lucide-react';
import { Button } from '@shared/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@shared/components/ui/card';
import { Input } from '@shared/components/ui/input';
import { Label } from '@shared/components/ui/label';
import { Textarea } from '@shared/components/ui/textarea';
import { useToast } from '@shared/hooks/use-toast';
import { getPacienteById } from '@shared/utils/mockData';
import { useState } from 'react';

const consultaSchema = z.object({
  fc: z.number().positive().optional(),
  fr: z.number().positive().optional(),
  temp: z.number().positive().optional(),
  peso: z.number().positive().optional(),
  examen: z.string().max(2000).optional(),
});

type ConsultaFormData = z.infer<typeof consultaSchema>;

export default function ConsultaForm() {
  const { pacienteId } = useParams();
  const navigate = useNavigate();
  const { toast } = useToast();
  const [diagnosticos, setDiagnosticos] = useState<string[]>([]);
  const [procedimientos, setProcedimientos] = useState<string[]>([]);
  const [nuevoDiagnostico, setNuevoDiagnostico] = useState('');
  const [nuevoProcedimiento, setNuevoProcedimiento] = useState('');

  const paciente = pacienteId ? getPacienteById(pacienteId) : undefined;

  const { register, handleSubmit, formState: { errors } } = useForm<ConsultaFormData>({
    resolver: zodResolver(consultaSchema),
  });

  const agregarDiagnostico = () => {
    if (nuevoDiagnostico.trim()) {
      setDiagnosticos([...diagnosticos, nuevoDiagnostico.trim()]);
      setNuevoDiagnostico('');
    }
  };

  const agregarProcedimiento = () => {
    if (nuevoProcedimiento.trim()) {
      setProcedimientos([...procedimientos, nuevoProcedimiento.trim()]);
      setNuevoProcedimiento('');
    }
  };

  const onSubmit = (data: ConsultaFormData) => {
    const consulta = {
      ...data,
      pacienteId,
      diagnosticos,
      procedimientos,
      fecha: new Date().toISOString(),
    };
    console.log('Guardar consulta:', consulta);
    toast({
      title: 'Consulta registrada',
      description: 'La consulta ha sido registrada exitosamente.',
    });
    navigate(`/historias/${pacienteId}`);
  };

  if (!paciente) {
    return (
      <div className="text-center py-12">
        <h3 className="text-lg font-medium">Paciente no encontrado</h3>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center gap-4">
        <Button variant="ghost" size="icon" onClick={() => navigate(`/historias/${pacienteId}`)}>
          <ArrowLeft className="h-4 w-4" />
        </Button>
        <div>
          <h1 className="text-3xl font-bold text-foreground">Nueva Consulta</h1>
          <p className="text-muted-foreground mt-1">Paciente: {paciente.nombre}</p>
        </div>
      </div>

      <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
        <Card>
          <CardHeader>
            <CardTitle>Signos Vitales</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="grid gap-4 md:grid-cols-4">
              <div className="space-y-2">
                <Label htmlFor="fc">FC (lpm)</Label>
                <Input
                  id="fc"
                  type="number"
                  {...register('fc', { valueAsNumber: true })}
                  placeholder="Frecuencia cardíaca"
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="fr">FR (rpm)</Label>
                <Input
                  id="fr"
                  type="number"
                  {...register('fr', { valueAsNumber: true })}
                  placeholder="Frecuencia respiratoria"
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="temp">Temperatura (°C)</Label>
                <Input
                  id="temp"
                  type="number"
                  step="0.1"
                  {...register('temp', { valueAsNumber: true })}
                  placeholder="Temperatura"
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="peso">Peso (kg)</Label>
                <Input
                  id="peso"
                  type="number"
                  step="0.1"
                  {...register('peso', { valueAsNumber: true })}
                  placeholder="Peso"
                />
              </div>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Examen Físico</CardTitle>
          </CardHeader>
          <CardContent>
            <Textarea
              {...register('examen')}
              placeholder="Descripción detallada del examen físico..."
              rows={6}
            />
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Diagnósticos</CardTitle>
          </CardHeader>
          <CardContent className="space-y-3">
            <div className="flex gap-2">
              <Input
                value={nuevoDiagnostico}
                onChange={(e) => setNuevoDiagnostico(e.target.value)}
                placeholder="Agregar diagnóstico..."
                onKeyPress={(e) => e.key === 'Enter' && (e.preventDefault(), agregarDiagnostico())}
              />
              <Button type="button" onClick={agregarDiagnostico} size="icon">
                <Plus className="h-4 w-4" />
              </Button>
            </div>
            {diagnosticos.length > 0 && (
              <div className="space-y-2">
                {diagnosticos.map((diag, index) => (
                  <div key={index} className="flex items-center gap-2 p-2 bg-accent/50 rounded-lg">
                    <span className="flex-1">{diag}</span>
                    <Button
                      type="button"
                      variant="ghost"
                      size="icon"
                      onClick={() => setDiagnosticos(diagnosticos.filter((_, i) => i !== index))}
                    >
                      <X className="h-4 w-4" />
                    </Button>
                  </div>
                ))}
              </div>
            )}
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Procedimientos</CardTitle>
          </CardHeader>
          <CardContent className="space-y-3">
            <div className="flex gap-2">
              <Input
                value={nuevoProcedimiento}
                onChange={(e) => setNuevoProcedimiento(e.target.value)}
                placeholder="Agregar procedimiento..."
                onKeyPress={(e) => e.key === 'Enter' && (e.preventDefault(), agregarProcedimiento())}
              />
              <Button type="button" onClick={agregarProcedimiento} size="icon">
                <Plus className="h-4 w-4" />
              </Button>
            </div>
            {procedimientos.length > 0 && (
              <div className="space-y-2">
                {procedimientos.map((proc, index) => (
                  <div key={index} className="flex items-center gap-2 p-2 bg-accent/50 rounded-lg">
                    <span className="flex-1">{proc}</span>
                    <Button
                      type="button"
                      variant="ghost"
                      size="icon"
                      onClick={() => setProcedimientos(procedimientos.filter((_, i) => i !== index))}
                    >
                      <X className="h-4 w-4" />
                    </Button>
                  </div>
                ))}
              </div>
            )}
          </CardContent>
        </Card>

        <div className="flex justify-end gap-3">
          <Button type="button" variant="outline" onClick={() => navigate(`/historias/${pacienteId}`)}>
            Cancelar
          </Button>
          <Button type="submit" className="gap-2">
            <Save className="h-4 w-4" />
            Guardar Consulta
          </Button>
        </div>
      </form>
    </div>
  );
}
