import axios from '@core/api/axios';
import { Paciente } from '@core/types';
import { Propietario } from '@core/types';
import { Cita } from '@core/types';

export interface ResultadoBusqueda {
  tipo: 'paciente' | 'propietario' | 'cita';
  id: string;
  titulo: string;
  subtitulo?: string;
  url: string;
  icono?: string;
}

export interface BusquedaGlobalResponse {
  pacientes: Paciente[];
  propietarios: Propietario[];
  citas: Cita[];
}

/**
 * Servicio para búsqueda global en toda la aplicación
 */
export const busquedaGlobalService = {
  /**
   * Realiza una búsqueda global en pacientes, propietarios y citas
   * @param query Término de búsqueda
   * @param limit Límite de resultados por tipo (default: 5)
   * @returns Resultados agrupados por tipo
   */
  async buscar(query: string, limit: number = 5): Promise<BusquedaGlobalResponse> {
    if (!query || query.trim().length < 2) {
      return { pacientes: [], propietarios: [], citas: [] };
    }

    const searchTerm = query.trim();

    try {
      // Búsquedas en paralelo
      const [pacientesRes, propietariosRes, citasRes] = await Promise.allSettled([
        axios.get('/pacientes/search', {
          params: {
            nombre: searchTerm,
            page: 0,
            size: limit,
            sort: 'nombre,asc',
          },
        }),
        axios.get('/propietarios/search', {
          params: {
            nombre: searchTerm,
            page: 0,
            size: limit,
            sort: 'nombre,asc',
          },
        }),
        // Para citas, buscamos por motivo o pacienteNombre si está disponible
        // Si no hay endpoint de búsqueda de texto, omitimos citas por ahora
        Promise.resolve({ status: 'rejected', reason: 'No search endpoint for citas' } as PromiseRejectedResult),
      ]);

      const pacientes = pacientesRes.status === 'fulfilled' 
        ? pacientesRes.value.data.content?.map((p: any) => ({
            ...p,
            id: String(p.id),
            propietarioId: String(p.propietarioId),
          })) || []
        : [];

      const propietarios = propietariosRes.status === 'fulfilled'
        ? propietariosRes.value.data.content?.map((p: any) => ({
            ...p,
            id: String(p.id),
          })) || []
        : [];

      // Citas: por ahora no incluimos en búsqueda global hasta tener endpoint de búsqueda de texto
      const citas: any[] = [];

      return { pacientes, propietarios, citas };
    } catch (error) {
      console.error('Error en búsqueda global:', error);
      return { pacientes: [], propietarios: [], citas: [] };
    }
  },

  /**
   * Convierte los resultados de búsqueda a un formato unificado
   * @param resultados Resultados de la búsqueda
   * @returns Array de resultados unificados
   */
  normalizarResultados(resultados: BusquedaGlobalResponse): ResultadoBusqueda[] {
    const normalizados: ResultadoBusqueda[] = [];

    // Pacientes
    resultados.pacientes.forEach((paciente) => {
      normalizados.push({
        tipo: 'paciente',
        id: paciente.id,
        titulo: paciente.nombre,
        subtitulo: `${paciente.especie}${paciente.raza ? ` • ${paciente.raza}` : ''}`,
        url: `/pacientes/${paciente.id}`,
        icono: '🐾',
      });
    });

    // Propietarios
    resultados.propietarios.forEach((propietario) => {
      normalizados.push({
        tipo: 'propietario',
        id: propietario.id,
        titulo: propietario.nombre,
        subtitulo: propietario.email || propietario.telefono || undefined,
        url: `/propietarios/${propietario.id}`,
        icono: '👤',
      });
    });

    // Citas
    resultados.citas.forEach((cita) => {
      const fecha = new Date(cita.fecha).toLocaleDateString('es-ES', {
        day: '2-digit',
        month: 'short',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
      });
      normalizados.push({
        tipo: 'cita',
        id: cita.id,
        titulo: cita.pacienteNombre || 'Paciente',
        subtitulo: `${fecha} • ${cita.motivo || 'Sin motivo'}`,
        url: `/agenda/${cita.id}`,
        icono: '📅',
      });
    });

    return normalizados;
  },
};

