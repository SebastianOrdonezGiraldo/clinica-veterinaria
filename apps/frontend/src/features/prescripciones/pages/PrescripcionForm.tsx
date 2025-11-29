import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { ArrowLeft, Plus, Trash2 } from 'lucide-react';
import { Button } from '@shared/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@shared/components/ui/card';
import { Input } from '@shared/components/ui/input';
import { Label } from '@shared/components/ui/label';
import { Textarea } from '@shared/components/ui/textarea';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@shared/components/ui/select';
import { Skeleton } from '@shared/components/ui/skeleton';
import { Breadcrumbs } from '@shared/components/common/Breadcrumbs';
import { prescripcionService, ItemPrescripcionDTO } from '@features/prescripciones/services/prescripcionService';
import { consultaService } from '@features/historias/services/consultaService';
import { pacienteService } from '@features/pacientes/services/pacienteService';
import { propietarioService } from '@features/propietarios/services/propietarioService';
import { Consulta, Paciente, Propietario } from '@core/types';
import { useLogger } from '@shared/hooks/useLogger';
import { useApiError } from '@shared/hooks/useApiError';

interface Medicamento {
  id: string;
  medicamento: string;
  presentacion?: string;
  dosis: string;
  frecuencia: string;
  duracionDias: number;
  viaAdministracion: 'ORAL' | 'INYECTABLE' | 'TOPICA' | 'OFTALMICA' | 'OTICA' | 'OTRA';
  indicaciones?: string;
}

export default function PrescripcionForm() {
  const logger = useLogger('PrescripcionForm');
  const { handleError, showSuccess } = useApiError();
  const navigate = useNavigate();
  const [consultaId, setConsultaId] = useState('');
  const [medicamentos, setMedicamentos] = useState<Medicamento[]>([
    { 
      id: '1', 
      medicamento: '', 
      dosis: '', 
      frecuencia: '', 
      duracionDias: 0, 
      viaAdministracion: 'ORAL' 
    }
  ]);
  const [indicaciones, setIndicaciones] = useState('');
  const [consultas, setConsultas] = useState<Consulta[]>([]);
  const [pacientes, setPacientes] = useState<Paciente[]>([]);
  const [propietarios, setPropietarios] = useState<Propietario[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [isLoadingData, setIsLoadingData] = useState(true);

  useEffect(() => {
    loadInitialData();
  }, []);

  const loadInitialData = async () => {
    try {
      setIsLoadingData(true);
      const [consultasData, pacientesData, propietariosData] = await Promise.all([
        consultaService.getAll(),
        pacienteService.getAll(),
        propietarioService.getAll(),
      ]);
      setConsultas(consultasData);
      setPacientes(pacientesData);
      setPropietarios(propietariosData);
    } catch (error: any) {
      logger.error('Error al cargar datos del formulario de prescripción', error, {
        action: 'loadData',
      });
      handleError(error, 'Error al cargar los datos');
    } finally {
      setIsLoadingData(false);
    }
  };

  const consultaSeleccionada = consultas.find(c => c.id === consultaId);
  const paciente = consultaSeleccionada ? pacientes.find(p => p.id === consultaSeleccionada.pacienteId) : undefined;
  const propietario = paciente ? propietarios.find(p => p.id === paciente.propietarioId) : undefined;

  const agregarMedicamento = () => {
    setMedicamentos([...medicamentos, {
      id: Date.now().toString(),
      medicamento: '',
      dosis: '',
      frecuencia: '',
      duracionDias: 0,
      viaAdministracion: 'ORAL'
    }]);
  };

  const eliminarMedicamento = (id: string) => {
    if (medicamentos.length > 1) {
      setMedicamentos(medicamentos.filter(m => m.id !== id));
    }
  };

  const actualizarMedicamento = (id: string, campo: keyof Medicamento, valor: string | number) => {
    setMedicamentos(medicamentos.map(m =>
      m.id === id ? { ...m, [campo]: valor } : m
    ));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!consultaId) {
      handleError(new Error('Debe seleccionar una consulta'), 'Debe seleccionar una consulta');
      return;
    }

    // Validar que al menos hay un medicamento con datos
    const medicamentosValidos = medicamentos.filter(m => 
      m.medicamento.trim() && m.dosis.trim() && m.frecuencia.trim() && m.duracionDias > 0
    );

    if (medicamentosValidos.length === 0) {
      handleError(new Error('Debe agregar al menos un medicamento con todos los datos completos'), 'Debe agregar al menos un medicamento con todos los datos completos');
      return;
    }

    try {
      setIsLoading(true);
      
      const items: ItemPrescripcionDTO[] = medicamentosValidos.map(m => ({
        medicamento: m.medicamento.trim(),
        presentacion: m.presentacion?.trim() || undefined,
        dosis: m.dosis.trim(),
        frecuencia: m.frecuencia.trim(),
        duracionDias: m.duracionDias,
        viaAdministracion: m.viaAdministracion,
        indicaciones: m.indicaciones?.trim() || undefined,
      }));

      await prescripcionService.create({
        consultaId,
        indicacionesGenerales: indicaciones.trim() || undefined,
        items,
      });

      showSuccess('Prescripción creada exitosamente');
      navigate('/prescripciones');
    } catch (error: any) {
      logger.error('Error al crear prescripción', error, {
        action: 'createPrescripcion',
        consultaId: selectedConsulta,
        pacienteId: selectedPaciente,
      });
      handleError(error, 'Error al crear la prescripción');
    } finally {
      setIsLoading(false);
    }
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
              {isLoadingData ? (
                <Skeleton className="h-10 w-full" />
              ) : (
                <Select value={consultaId} onValueChange={setConsultaId}>
                  <SelectTrigger>
                    <SelectValue placeholder="Seleccionar consulta" />
                  </SelectTrigger>
                  <SelectContent>
                    {consultas.map((consulta) => {
                      const pac = pacientes.find(p => p.id === consulta.pacienteId);
                      return (
                        <SelectItem key={consulta.id} value={consulta.id}>
                          {new Date(consulta.fecha).toLocaleDateString('es-ES')} - {pac?.nombre || 'N/A'}
                        </SelectItem>
                      );
                    })}
                  </SelectContent>
                </Select>
              )}
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
                      value={med.medicamento}
                      onChange={(e) => actualizarMedicamento(med.id, 'medicamento', e.target.value)}
                      placeholder="Ej: Amoxicilina"
                      required
                    />
                  </div>
                  <div>
                    <Label>Presentación</Label>
                    <Input
                      value={med.presentacion || ''}
                      onChange={(e) => actualizarMedicamento(med.id, 'presentacion', e.target.value)}
                      placeholder="Ej: Tabletas 250mg"
                    />
                  </div>
                  <div>
                    <Label>Vía de Administración *</Label>
                    <Select
                      value={med.viaAdministracion}
                      onValueChange={(value) => actualizarMedicamento(med.id, 'viaAdministracion', value as any)}
                    >
                      <SelectTrigger>
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="ORAL">Oral</SelectItem>
                        <SelectItem value="INYECTABLE">Inyectable</SelectItem>
                        <SelectItem value="TOPICA">Tópica</SelectItem>
                        <SelectItem value="OFTALMICA">Oftálmica</SelectItem>
                        <SelectItem value="OTICA">Ótica</SelectItem>
                        <SelectItem value="OTRA">Otra</SelectItem>
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
                    <Label>Duración (días) *</Label>
                    <Input
                      type="number"
                      min="1"
                      value={med.duracionDias || ''}
                      onChange={(e) => actualizarMedicamento(med.id, 'duracionDias', e.target.value ? parseInt(e.target.value, 10) : 0)}
                      placeholder="Ej: 7"
                      required
                    />
                  </div>
                  <div className="md:col-span-2">
                    <Label>Indicaciones</Label>
                    <Textarea
                      value={med.indicaciones || ''}
                      onChange={(e) => actualizarMedicamento(med.id, 'indicaciones', e.target.value)}
                      placeholder="Indicaciones específicas para este medicamento"
                      rows={2}
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
          <Button type="button" variant="outline" onClick={() => navigate('/prescripciones')} disabled={isLoading}>
            Cancelar
          </Button>
          <Button type="submit" disabled={isLoading || isLoadingData}>
            {isLoading ? 'Creando...' : 'Crear Prescripción'}
          </Button>
        </div>
      </form>
    </div>
  );
}
