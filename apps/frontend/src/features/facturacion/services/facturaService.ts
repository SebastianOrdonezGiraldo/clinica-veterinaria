import axios from '@core/api/axios';
import { PageResponse, PageParams } from '@core/types';

export type EstadoFactura = 'PENDIENTE' | 'PARCIAL' | 'PAGADA' | 'CANCELADA' | 'VENCIDA';

export interface ItemFactura {
  id?: string;
  descripcion: string;
  tipoItem?: string;
  codigoProducto?: string;
  cantidad: number;
  precioUnitario: number;
  descuento?: number;
  subtotal?: number;
  orden?: number;
}

export interface Pago {
  id?: string;
  monto: number;
  fechaPago: string;
  metodoPago?: string;
  referencia?: string;
  observaciones?: string;
  facturaId?: string;
  usuarioId?: string;
  usuarioNombre?: string;
  createdAt?: string;
}

export interface Factura {
  id: string;
  numeroFactura: string;
  fechaEmision: string;
  fechaVencimiento?: string;
  subtotal: number;
  descuento?: number;
  impuesto?: number;
  total: number;
  montoPagado: number;
  montoPendiente: number;
  observaciones?: string;
  estado: EstadoFactura;
  propietarioId: string;
  propietarioNombre?: string;
  consultaId?: string;
  items: ItemFactura[];
  pagos: Pago[];
  createdAt?: string;
  updatedAt?: string;
}

export interface FacturaCreate {
  propietarioId: string;
  consultaId?: string;
  fechaEmision?: string;
  fechaVencimiento?: string;
  descuento?: number;
  impuesto?: number;
  observaciones?: string;
  items: ItemFactura[];
}

export interface FacturaSearchParams extends PageParams {
  propietarioId?: string;
  estado?: EstadoFactura;
  fechaInicio?: string;
  fechaFin?: string;
}

export interface EstadisticasFinancieras {
  totalFacturado: number;
  totalPagado: number;
  totalPendiente: number;
  fechaInicio: string;
  fechaFin: string;
}

const normalizeFactura = (factura: any): Factura => ({
  ...factura,
  id: String(factura.id),
  propietarioId: String(factura.propietarioId),
  consultaId: factura.consultaId ? String(factura.consultaId) : undefined,
  items: factura.items?.map((item: any) => ({
    ...item,
    id: item.id ? String(item.id) : undefined,
  })) || [],
  pagos: factura.pagos?.map((pago: any) => ({
    ...pago,
    id: pago.id ? String(pago.id) : undefined,
    facturaId: pago.facturaId ? String(pago.facturaId) : undefined,
    usuarioId: pago.usuarioId ? String(pago.usuarioId) : undefined,
  })) || [],
});

export const facturaService = {
  async getAll(params: FacturaSearchParams = {}): Promise<PageResponse<Factura>> {
    const response = await axios.get<PageResponse<any>>('/facturas', {
      params: {
        page: params.page ?? 0,
        size: params.size ?? 20,
        sort: params.sort || 'fechaEmision,desc',
        propietarioId: params.propietarioId ? Number(params.propietarioId) : undefined,
        estado: params.estado,
        fechaInicio: params.fechaInicio,
        fechaFin: params.fechaFin,
      },
    });
    return {
      ...response.data,
      content: response.data.content.map(normalizeFactura),
    };
  },

  async getById(id: string): Promise<Factura> {
    const response = await axios.get<any>(`/facturas/${id}`);
    return normalizeFactura(response.data);
  },

  async getByNumeroFactura(numeroFactura: string): Promise<Factura> {
    const response = await axios.get<any>(`/facturas/numero/${numeroFactura}`);
    return normalizeFactura(response.data);
  },

  async getByPropietario(propietarioId: string, params: PageParams = {}): Promise<PageResponse<Factura>> {
    const response = await axios.get<PageResponse<any>>(`/facturas/propietario/${propietarioId}`, {
      params: {
        page: params.page ?? 0,
        size: params.size ?? 20,
        sort: params.sort || 'fechaEmision,desc',
      },
    });
    return {
      ...response.data,
      content: response.data.content.map(normalizeFactura),
    };
  },

  async create(data: FacturaCreate): Promise<Factura> {
    const payload = {
      ...data,
      propietarioId: Number(data.propietarioId),
      consultaId: data.consultaId ? Number(data.consultaId) : undefined,
      items: data.items.map(item => ({
        ...item,
        cantidad: Number(item.cantidad),
        precioUnitario: Number(item.precioUnitario),
        descuento: item.descuento ? Number(item.descuento) : undefined,
      })),
    };
    const response = await axios.post<any>('/facturas', payload);
    return normalizeFactura(response.data);
  },

  async createFromConsulta(consultaId: string, itemsAdicionales?: ItemFactura[]): Promise<Factura> {
    const payload = itemsAdicionales?.map(item => ({
      ...item,
      cantidad: Number(item.cantidad),
      precioUnitario: Number(item.precioUnitario),
      descuento: item.descuento ? Number(item.descuento) : undefined,
    }));
    const response = await axios.post<any>(`/facturas/desde-consulta/${consultaId}`, payload || []);
    return normalizeFactura(response.data);
  },

  async update(id: string, data: Partial<FacturaCreate>): Promise<Factura> {
    const payload = {
      ...data,
      propietarioId: data.propietarioId ? Number(data.propietarioId) : undefined,
      consultaId: data.consultaId ? Number(data.consultaId) : undefined,
    };
    const response = await axios.put<any>(`/facturas/${id}`, payload);
    return normalizeFactura(response.data);
  },

  async cancel(id: string): Promise<void> {
    await axios.post(`/facturas/${id}/cancelar`);
  },

  async registrarPago(id: string, pago: Omit<Pago, 'id' | 'facturaId' | 'usuarioId' | 'usuarioNombre' | 'createdAt'>): Promise<Pago> {
    const payload = {
      ...pago,
      monto: Number(pago.monto),
    };
    const response = await axios.post<any>(`/facturas/${id}/pagos`, payload);
    return {
      ...response.data,
      id: response.data.id ? String(response.data.id) : undefined,
      facturaId: response.data.facturaId ? String(response.data.facturaId) : undefined,
      usuarioId: response.data.usuarioId ? String(response.data.usuarioId) : undefined,
    };
  },

  async getEstadisticas(fechaInicio: string, fechaFin: string): Promise<EstadisticasFinancieras> {
    const response = await axios.get<EstadisticasFinancieras>('/facturas/estadisticas', {
      params: { fechaInicio, fechaFin },
    });
    return response.data;
  },

  async downloadPdf(id: string): Promise<Blob> {
    const response = await axios.get(`/facturas/${id}/pdf`, {
      responseType: 'blob',
    });
    return response.data;
  },
};

