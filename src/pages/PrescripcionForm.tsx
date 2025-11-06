import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { ArrowLeft, Plus, Trash2 } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Breadcrumbs } from '@/components/Breadcrumbs';
import { mockConsultas, mockPacientes, mockPropietarios } from '@/lib/mockData';
import { toast } from 'sonner';

interface Medicamento {
  id: string;
  nombre: string;
  dosis: string;
  frecuencia: string;
  duracion: string;
  via: string;
}

export default function PrescripcionForm() {
  const navigate = useNavigate();
  const [consultaId, setConsultaId] = useState('');
  const [medicamentos, setMedicamentos] = useState<Medicamento[]>([
    { id: '1', nombre: '', dosis: '', frecuencia: '', duracion: '', via: 'Oral' }
  ]);
  const [indicaciones, setIndicaciones] = useState('');

  const consultaSeleccionada = mockConsultas.find(c => c.id === consultaId);
  const paciente = consultaSeleccionada ? mockPacientes.find(p => p.id === consultaSeleccionada.pacienteId) : undefined;
  const propietario = paciente ? mockPropietarios.find(p => p.id === paciente.propietarioId) : undefined;

  const agregarMedicamento = () => {
    setMedicamentos([...medicamentos, {
      id: Date.now().toString(),
      nombre: '',
      dosis: '',
      frecuencia: '',
      duracion: '',
      via: 'Oral'
    }]);
  };

  const eliminarMedicamento = (id: string) => {
    if (medicamentos.length > 1) {
      setMedicamentos(medicamentos.filter(m => m.id !== id));
    }
  };

  const actualizarMedicamento = (id: string, campo: keyof Medicamento, valor: string) => {
    setMedicamentos(medicamentos.map(m =>
      m.id === id ? { ...m, [campo]: valor } : m
    ));
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!consultaId) {
      toast.error('Debe seleccionar una consulta');
      return;
    }
    toast.success('Prescripción creada exitosamente');
    navigate('/prescripciones');
  };

  return (
    <div className="space-y-6">
      <Breadcrumbs items={[
        { label: 'Prescripciones', href: '/prescripciones' },
        { label: 'Nueva Prescripción' }
      ]} />

      <div className="flex items-center gap-4">
        <Button variant="ghost" size="icon" onClick={() => navigate('/prescripciones')}>
          <ArrowLeft className="h-5 w-5" />
        </Button>
        <div>
          <h1 className="text-3xl font-bold text-foreground">Nueva Prescripción</h1>
          <p className="text-muted-foreground mt-1">Complete los datos de la receta médica</p>
        </div>
      </div>

      <form onSubmit={handleSubmit} className="space-y-6">
        <Card>
          <CardHeader>
            <CardTitle>Consulta y Paciente</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div>
              <Label>Consulta *</Label>
              <Select value={consultaId} onValueChange={setConsultaId}>
                <SelectTrigger>
                  <SelectValue placeholder="Seleccionar consulta" />
                </SelectTrigger>
                <SelectContent>
                  {mockConsultas.map((consulta) => {
                    const pac = mockPacientes.find(p => p.id === consulta.pacienteId);
                    return (
                      <SelectItem key={consulta.id} value={consulta.id}>
                        {new Date(consulta.fecha).toLocaleDateString()} - {pac?.nombre}
                      </SelectItem>
                    );
                  })}
                </SelectContent>
              </Select>
            </div>

            {paciente && (
              <div className="grid gap-4 p-4 rounded-lg bg-accent/50">
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <p className="text-sm text-muted-foreground">Paciente</p>
                    <p className="font-medium">{paciente.nombre}</p>
                  </div>
                  <div>
                    <p className="text-sm text-muted-foreground">Especie</p>
                    <p className="font-medium">{paciente.especie}</p>
                  </div>
                  <div>
                    <p className="text-sm text-muted-foreground">Propietario</p>
                    <p className="font-medium">{propietario?.nombre}</p>
                  </div>
                  <div>
                    <p className="text-sm text-muted-foreground">Peso</p>
                    <p className="font-medium">{paciente.pesoKg} kg</p>
                  </div>
                </div>
              </div>
            )}
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <div className="flex items-center justify-between">
              <CardTitle>Medicamentos</CardTitle>
              <Button type="button" variant="outline" size="sm" onClick={agregarMedicamento}>
                <Plus className="h-4 w-4 mr-2" />
                Agregar Medicamento
              </Button>
            </div>
          </CardHeader>
          <CardContent className="space-y-4">
            {medicamentos.map((med, index) => (
              <div key={med.id} className="p-4 rounded-lg border border-border space-y-4">
                <div className="flex items-center justify-between">
                  <h4 className="font-medium">Medicamento {index + 1}</h4>
                  {medicamentos.length > 1 && (
                    <Button
                      type="button"
                      variant="ghost"
                      size="sm"
                      onClick={() => eliminarMedicamento(med.id)}
                    >
                      <Trash2 className="h-4 w-4 text-destructive" />
                    </Button>
                  )}
                </div>

                <div className="grid gap-4 md:grid-cols-2">
                  <div>
                    <Label>Nombre del Medicamento *</Label>
                    <Input
                      value={med.nombre}
                      onChange={(e) => actualizarMedicamento(med.id, 'nombre', e.target.value)}
                      placeholder="Ej: Amoxicilina"
                      required
                    />
                  </div>
                  <div>
                    <Label>Vía de Administración *</Label>
                    <Select
                      value={med.via}
                      onValueChange={(value) => actualizarMedicamento(med.id, 'via', value)}
                    >
                      <SelectTrigger>
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="Oral">Oral</SelectItem>
                        <SelectItem value="Inyectable">Inyectable</SelectItem>
                        <SelectItem value="Tópica">Tópica</SelectItem>
                        <SelectItem value="Oftálmica">Oftálmica</SelectItem>
                        <SelectItem value="Ótica">Ótica</SelectItem>
                      </SelectContent>
                    </Select>
                  </div>
                  <div>
                    <Label>Dosis *</Label>
                    <Input
                      value={med.dosis}
                      onChange={(e) => actualizarMedicamento(med.id, 'dosis', e.target.value)}
                      placeholder="Ej: 250mg"
                      required
                    />
                  </div>
                  <div>
                    <Label>Frecuencia *</Label>
                    <Input
                      value={med.frecuencia}
                      onChange={(e) => actualizarMedicamento(med.id, 'frecuencia', e.target.value)}
                      placeholder="Ej: Cada 12 horas"
                      required
                    />
                  </div>
                  <div>
                    <Label>Duración *</Label>
                    <Input
                      value={med.duracion}
                      onChange={(e) => actualizarMedicamento(med.id, 'duracion', e.target.value)}
                      placeholder="Ej: 7 días"
                      required
                    />
                  </div>
                </div>
              </div>
            ))}
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Indicaciones Adicionales</CardTitle>
          </CardHeader>
          <CardContent>
            <Textarea
              value={indicaciones}
              onChange={(e) => setIndicaciones(e.target.value)}
              placeholder="Indicaciones generales, cuidados especiales, contraindicaciones, etc."
              rows={5}
            />
          </CardContent>
        </Card>

        <div className="flex justify-end gap-3">
          <Button type="button" variant="outline" onClick={() => navigate('/prescripciones')}>
            Cancelar
          </Button>
          <Button type="submit">
            Crear Prescripción
          </Button>
        </div>
      </form>
    </div>
  );
}
