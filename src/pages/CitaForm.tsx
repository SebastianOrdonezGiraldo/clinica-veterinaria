import { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { ArrowLeft, Save, Calendar as CalendarIcon } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Textarea } from '@/components/ui/textarea';
import { toast } from 'sonner';
import { mockCitas, saveCita, mockPacientes, mockPropietarios, mockUsuarios } from '@/lib/mockData';
import { Cita } from '@/types';

export default function CitaForm() {
  const { id } = useParams();
  const navigate = useNavigate();
  const isEdit = id && id !== 'nuevo';

  const [formData, setFormData] = useState<Partial<Cita>>({
    pacienteId: '',
    propietarioId: '',
    profesionalId: '',
    fecha: '',
    estado: 'Pendiente',
    motivo: '',
  });

  const [loading, setLoading] = useState(false);
  const [errors, setErrors] = useState<Record<string, string>>({});

  // Filtrar solo veterinarios
  const veterinarios = mockUsuarios.filter(u => u.rol === 'VET' || u.rol === 'ADMIN');

  useEffect(() => {
    if (isEdit) {
      const cita = mockCitas.find(c => c.id === id);
      if (cita) {
        setFormData({
          pacienteId: cita.pacienteId,
          propietarioId: cita.propietarioId,
          profesionalId: cita.profesionalId,
          fecha: cita.fecha.slice(0, 16), // Formato para datetime-local
          estado: cita.estado,
          motivo: cita.motivo || '',
        });
      } else {
        toast.error('Cita no encontrada');
        navigate('/agenda');
      }
    } else {
      // Fecha por defecto: mañana a las 9:00
      const tomorrow = new Date();
      tomorrow.setDate(tomorrow.getDate() + 1);
      tomorrow.setHours(9, 0, 0, 0);
      setFormData(prev => ({
        ...prev,
        fecha: tomorrow.toISOString().slice(0, 16),
      }));
    }
  }, [id, isEdit, navigate]);

  // Cuando se selecciona un paciente, auto-seleccionar su propietario
  useEffect(() => {
    if (formData.pacienteId) {
      const paciente = mockPacientes.find(p => p.id === formData.pacienteId);
      if (paciente && paciente.propietarioId !== formData.propietarioId) {
        setFormData(prev => ({ ...prev, propietarioId: paciente.propietarioId }));
      }
    }
  }, [formData.pacienteId]);

  const validateForm = (): boolean => {
    const newErrors: Record<string, string> = {};

    if (!formData.pacienteId) {
      newErrors.pacienteId = 'Debe seleccionar un paciente';
    }

    if (!formData.profesionalId) {
      newErrors.profesionalId = 'Debe seleccionar un veterinario';
    }

    if (!formData.fecha) {
      newErrors.fecha = 'La fecha y hora son obligatorias';
    } else {
      const citaDate = new Date(formData.fecha);
      const now = new Date();
      if (citaDate < now && !isEdit) {
        newErrors.fecha = 'La fecha no puede ser en el pasado';
      }
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

      const citaData = {
        ...(isEdit && { id }),
        pacienteId: formData.pacienteId!,
        propietarioId: formData.propietarioId!,
        profesionalId: formData.profesionalId!,
        fecha: new Date(formData.fecha!).toISOString(),
        estado: formData.estado as 'Pendiente' | 'Confirmada' | 'Cancelada' | 'Atendida',
        motivo: formData.motivo,
      };

      saveCita(citaData);

      toast.success(isEdit ? 'Cita actualizada correctamente' : 'Cita creada correctamente');
      navigate('/agenda');
    } catch (error) {
      toast.error('Error al guardar la cita');
    } finally {
      setLoading(false);
    }
  };

  const handleChange = (field: keyof Cita, value: any) => {
    setFormData(prev => ({ ...prev, [field]: value }));
    // Limpiar error del campo
    if (errors[field]) {
      setErrors(prev => ({ ...prev, [field]: '' }));
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center gap-4">
        <Button variant="ghost" size="icon" onClick={() => navigate('/agenda')}>
          <ArrowLeft className="h-5 w-5" />
        </Button>
        <div>
          <h1 className="text-3xl font-bold text-foreground">
            {isEdit ? 'Editar Cita' : 'Nueva Cita'}
          </h1>
          <p className="text-muted-foreground mt-1">
            {isEdit ? 'Actualiza la información de la cita' : 'Agenda una nueva cita médica'}
          </p>
        </div>
      </div>

      <form onSubmit={handleSubmit}>
        <Card>
          <CardHeader>
            <CardTitle>Información de la Cita</CardTitle>
          </CardHeader>
          <CardContent className="space-y-6">
            <div className="grid gap-6 md:grid-cols-2">
              {/* Paciente */}
              <div className="space-y-2">
                <Label htmlFor="paciente">
                  Paciente <span className="text-destructive">*</span>
                </Label>
                <Select
                  value={formData.pacienteId}
                  onValueChange={(value) => handleChange('pacienteId', value)}
                >
                  <SelectTrigger className={errors.pacienteId ? 'border-destructive' : ''}>
                    <SelectValue placeholder="Selecciona un paciente" />
                  </SelectTrigger>
                  <SelectContent>
                    {mockPacientes.map((paciente) => {
                      const propietario = mockPropietarios.find(p => p.id === paciente.propietarioId);
                      return (
                        <SelectItem key={paciente.id} value={paciente.id}>
                          {paciente.nombre} - {propietario?.nombre}
                        </SelectItem>
                      );
                    })}
                  </SelectContent>
                </Select>
                {errors.pacienteId && (
                  <p className="text-sm text-destructive">{errors.pacienteId}</p>
                )}
              </div>

              {/* Veterinario */}
              <div className="space-y-2">
                <Label htmlFor="profesional">
                  Veterinario <span className="text-destructive">*</span>
                </Label>
                <Select
                  value={formData.profesionalId}
                  onValueChange={(value) => handleChange('profesionalId', value)}
                >
                  <SelectTrigger className={errors.profesionalId ? 'border-destructive' : ''}>
                    <SelectValue placeholder="Selecciona un veterinario" />
                  </SelectTrigger>
                  <SelectContent>
                    {veterinarios.map((vet) => (
                      <SelectItem key={vet.id} value={vet.id}>
                        {vet.nombre}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
                {errors.profesionalId && (
                  <p className="text-sm text-destructive">{errors.profesionalId}</p>
                )}
              </div>

              {/* Fecha y Hora */}
              <div className="space-y-2">
                <Label htmlFor="fecha">
                  Fecha y Hora <span className="text-destructive">*</span>
                </Label>
                <div className="relative">
                  <CalendarIcon className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-muted-foreground pointer-events-none" />
                  <Input
                    id="fecha"
                    type="datetime-local"
                    value={formData.fecha}
                    onChange={(e) => handleChange('fecha', e.target.value)}
                    className={`pl-10 ${errors.fecha ? 'border-destructive' : ''}`}
                  />
                </div>
                {errors.fecha && (
                  <p className="text-sm text-destructive">{errors.fecha}</p>
                )}
              </div>

              {/* Estado */}
              <div className="space-y-2">
                <Label htmlFor="estado">Estado</Label>
                <Select
                  value={formData.estado}
                  onValueChange={(value) => handleChange('estado', value)}
                >
                  <SelectTrigger>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="Pendiente">Pendiente</SelectItem>
                    <SelectItem value="Confirmada">Confirmada</SelectItem>
                    <SelectItem value="Atendida">Atendida</SelectItem>
                    <SelectItem value="Cancelada">Cancelada</SelectItem>
                  </SelectContent>
                </Select>
              </div>
            </div>

            {/* Motivo */}
            <div className="space-y-2">
              <Label htmlFor="motivo">Motivo de la Consulta</Label>
              <Textarea
                id="motivo"
                value={formData.motivo}
                onChange={(e) => handleChange('motivo', e.target.value)}
                placeholder="Ej: Vacunación anual, control general, revisión..."
                rows={3}
              />
            </div>

            {/* Botones */}
            <div className="flex gap-3 justify-end pt-4">
              <Button
                type="button"
                variant="outline"
                onClick={() => navigate('/agenda')}
                disabled={loading}
              >
                Cancelar
              </Button>
              <Button type="submit" disabled={loading} className="gap-2">
                <Save className="h-4 w-4" />
                {loading ? 'Guardando...' : isEdit ? 'Actualizar' : 'Crear Cita'}
              </Button>
            </div>
          </CardContent>
        </Card>
      </form>
    </div>
  );
}

