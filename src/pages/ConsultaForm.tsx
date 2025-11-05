import { useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { ArrowLeft, Save, Plus, X } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Textarea } from '@/components/ui/textarea';
import { Badge } from '@/components/ui/badge';
import { toast } from 'sonner';
import { getPacienteById, saveConsulta } from '@/lib/mockData';
import { useAuth } from '@/contexts/AuthContext';
import { Consulta, SignosVitales } from '@/types';

export default function ConsultaForm() {
  const { pacienteId } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();
  const paciente = pacienteId ? getPacienteById(pacienteId) : undefined;

  const [formData, setFormData] = useState<Partial<Consulta>>({
    pacienteId: pacienteId || '',
    profesionalId: user?.id || '',
    fecha: new Date().toISOString(),
    signosVitales: {
      fc: undefined,
      fr: undefined,
      temp: undefined,
      peso: undefined,
    },
    examen: '',
    diagnosticos: [],
    procedimientos: [],
  });

  const [loading, setLoading] = useState(false);
  const [errors, setErrors] = useState<Record<string, string>>({});
  
  // Estados temporales para agregar diagnósticos y procedimientos
  const [nuevoDiagnostico, setNuevoDiagnostico] = useState('');
  const [nuevoProcedimiento, setNuevoProcedimiento] = useState('');

  if (!paciente) {
    return (
      <div className="text-center py-12">
        <h2 className="text-2xl font-bold">Paciente no encontrado</h2>
        <Button onClick={() => navigate('/historias')} className="mt-4">
          Volver a Historias
        </Button>
      </div>
    );
  }

  const validateForm = (): boolean => {
    const newErrors: Record<string, string> = {};

    if (!formData.examen?.trim()) {
      newErrors.examen = 'El examen físico es obligatorio';
    }

    if (formData.signosVitales?.temp && (formData.signosVitales.temp < 35 || formData.signosVitales.temp > 42)) {
      newErrors.temp = 'Temperatura fuera de rango normal (35-42°C)';
    }

    if (formData.signosVitales?.fc && formData.signosVitales.fc < 0) {
      newErrors.fc = 'Frecuencia cardíaca inválida';
    }

    if (formData.signosVitales?.fr && formData.signosVitales.fr < 0) {
      newErrors.fr = 'Frecuencia respiratoria inválida';
    }

    if (formData.signosVitales?.peso && formData.signosVitales.peso <= 0) {
      newErrors.peso = 'Peso inválido';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!validateForm()) {
      toast.error('Por favor corrige los errores del formulario');
      return;
    }

    setLoading(true);

    try {
      // Simular delay de red
      await new Promise(resolve => setTimeout(resolve, 500));

      const consultaData: Omit<Consulta, 'id'> = {
        pacienteId: formData.pacienteId!,
        profesionalId: formData.profesionalId!,
        fecha: formData.fecha!,
        signosVitales: formData.signosVitales,
        examen: formData.examen!,
        diagnosticos: formData.diagnosticos,
        procedimientos: formData.procedimientos,
      };

      saveConsulta(consultaData);

      toast.success('Consulta registrada correctamente');
      navigate(`/historias/${pacienteId}`);
    } catch (error) {
      toast.error('Error al guardar la consulta');
    } finally {
      setLoading(false);
    }
  };

  const handleSignosVitalesChange = (field: keyof SignosVitales, value: string) => {
    const numValue = value ? parseFloat(value) : undefined;
    setFormData(prev => ({
      ...prev,
      signosVitales: {
        ...prev.signosVitales,
        [field]: numValue,
      },
    }));
    if (errors[field]) {
      setErrors(prev => ({ ...prev, [field]: '' }));
    }
  };

  const agregarDiagnostico = () => {
    if (nuevoDiagnostico.trim()) {
      setFormData(prev => ({
        ...prev,
        diagnosticos: [...(prev.diagnosticos || []), nuevoDiagnostico.trim()],
      }));
      setNuevoDiagnostico('');
    }
  };

  const eliminarDiagnostico = (index: number) => {
    setFormData(prev => ({
      ...prev,
      diagnosticos: prev.diagnosticos?.filter((_, i) => i !== index),
    }));
  };

  const agregarProcedimiento = () => {
    if (nuevoProcedimiento.trim()) {
      setFormData(prev => ({
        ...prev,
        procedimientos: [...(prev.procedimientos || []), nuevoProcedimiento.trim()],
      }));
      setNuevoProcedimiento('');
    }
  };

  const eliminarProcedimiento = (index: number) => {
    setFormData(prev => ({
      ...prev,
      procedimientos: prev.procedimientos?.filter((_, i) => i !== index),
    }));
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center gap-4">
        <Button variant="ghost" size="icon" onClick={() => navigate(`/historias/${pacienteId}`)}>
          <ArrowLeft className="h-5 w-5" />
        </Button>
        <div>
          <h1 className="text-3xl font-bold text-foreground">Nueva Consulta</h1>
          <p className="text-muted-foreground mt-1">
            Paciente: {paciente.nombre} • {paciente.especie}
          </p>
        </div>
      </div>

      <form onSubmit={handleSubmit} className="space-y-6">
        {/* Signos Vitales */}
        <Card>
          <CardHeader>
            <CardTitle>Signos Vitales</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="grid gap-4 md:grid-cols-4">
              <div className="space-y-2">
                <Label htmlFor="fc">Frecuencia Cardíaca (lpm)</Label>
                <Input
                  id="fc"
                  type="number"
                  min="0"
                  value={formData.signosVitales?.fc || ''}
                  onChange={(e) => handleSignosVitalesChange('fc', e.target.value)}
                  placeholder="90"
                  className={errors.fc ? 'border-destructive' : ''}
                />
                {errors.fc && <p className="text-sm text-destructive">{errors.fc}</p>}
              </div>

              <div className="space-y-2">
                <Label htmlFor="fr">Frecuencia Respiratoria (rpm)</Label>
                <Input
                  id="fr"
                  type="number"
                  min="0"
                  value={formData.signosVitales?.fr || ''}
                  onChange={(e) => handleSignosVitalesChange('fr', e.target.value)}
                  placeholder="25"
                  className={errors.fr ? 'border-destructive' : ''}
                />
                {errors.fr && <p className="text-sm text-destructive">{errors.fr}</p>}
              </div>

              <div className="space-y-2">
                <Label htmlFor="temp">Temperatura (°C)</Label>
                <Input
                  id="temp"
                  type="number"
                  step="0.1"
                  min="35"
                  max="42"
                  value={formData.signosVitales?.temp || ''}
                  onChange={(e) => handleSignosVitalesChange('temp', e.target.value)}
                  placeholder="38.5"
                  className={errors.temp ? 'border-destructive' : ''}
                />
                {errors.temp && <p className="text-sm text-destructive">{errors.temp}</p>}
              </div>

              <div className="space-y-2">
                <Label htmlFor="peso">Peso (kg)</Label>
                <Input
                  id="peso"
                  type="number"
                  step="0.1"
                  min="0"
                  value={formData.signosVitales?.peso || ''}
                  onChange={(e) => handleSignosVitalesChange('peso', e.target.value)}
                  placeholder="28.5"
                  className={errors.peso ? 'border-destructive' : ''}
                />
                {errors.peso && <p className="text-sm text-destructive">{errors.peso}</p>}
              </div>
            </div>
          </CardContent>
        </Card>

        {/* Examen Físico */}
        <Card>
          <CardHeader>
            <CardTitle>
              Examen Físico <span className="text-destructive">*</span>
            </CardTitle>
          </CardHeader>
          <CardContent>
            <Textarea
              value={formData.examen}
              onChange={(e) => {
                setFormData(prev => ({ ...prev, examen: e.target.value }));
                if (errors.examen) setErrors(prev => ({ ...prev, examen: '' }));
              }}
              placeholder="Describe el estado general del paciente, hallazgos del examen físico..."
              rows={5}
              className={errors.examen ? 'border-destructive' : ''}
            />
            {errors.examen && <p className="text-sm text-destructive mt-2">{errors.examen}</p>}
          </CardContent>
        </Card>

        {/* Diagnósticos */}
        <Card>
          <CardHeader>
            <CardTitle>Diagnósticos</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="flex gap-2">
              <Input
                value={nuevoDiagnostico}
                onChange={(e) => setNuevoDiagnostico(e.target.value)}
                placeholder="Ej: Otitis leve oído derecho"
                onKeyPress={(e) => e.key === 'Enter' && (e.preventDefault(), agregarDiagnostico())}
              />
              <Button type="button" onClick={agregarDiagnostico} size="icon">
                <Plus className="h-4 w-4" />
              </Button>
            </div>

            {formData.diagnosticos && formData.diagnosticos.length > 0 && (
              <div className="flex flex-wrap gap-2">
                {formData.diagnosticos.map((diag, index) => (
                  <Badge key={index} variant="outline" className="gap-2 pr-1">
                    {diag}
                    <button
                      type="button"
                      onClick={() => eliminarDiagnostico(index)}
                      className="ml-1 hover:bg-destructive/20 rounded-full p-0.5"
                    >
                      <X className="h-3 w-3" />
                    </button>
                  </Badge>
                ))}
              </div>
            )}
          </CardContent>
        </Card>

        {/* Procedimientos */}
        <Card>
          <CardHeader>
            <CardTitle>Procedimientos Realizados</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="flex gap-2">
              <Input
                value={nuevoProcedimiento}
                onChange={(e) => setNuevoProcedimiento(e.target.value)}
                placeholder="Ej: Vacuna antirrábica"
                onKeyPress={(e) => e.key === 'Enter' && (e.preventDefault(), agregarProcedimiento())}
              />
              <Button type="button" onClick={agregarProcedimiento} size="icon">
                <Plus className="h-4 w-4" />
              </Button>
            </div>

            {formData.procedimientos && formData.procedimientos.length > 0 && (
              <div className="space-y-2">
                {formData.procedimientos.map((proc, index) => (
                  <div key={index} className="flex items-center justify-between p-2 rounded-lg border">
                    <span className="text-sm">{proc}</span>
                    <Button
                      type="button"
                      variant="ghost"
                      size="sm"
                      onClick={() => eliminarProcedimiento(index)}
                    >
                      <X className="h-4 w-4" />
                    </Button>
                  </div>
                ))}
              </div>
            )}
          </CardContent>
        </Card>

        {/* Botones */}
        <div className="flex gap-3 justify-end">
          <Button
            type="button"
            variant="outline"
            onClick={() => navigate(`/historias/${pacienteId}`)}
            disabled={loading}
          >
            Cancelar
          </Button>
          <Button type="submit" disabled={loading} className="gap-2">
            <Save className="h-4 w-4" />
            {loading ? 'Guardando...' : 'Guardar Consulta'}
          </Button>
        </div>
      </form>
    </div>
  );
}

