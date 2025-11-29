import type { Meta, StoryObj } from '@storybook/react';
import {
  Table,
  TableHeader,
  TableBody,
  TableFooter,
  TableRow,
  TableHead,
  TableCell,
  TableCaption,
} from './table';
import { Badge } from './badge';

/**
 * Table es un conjunto de componentes para crear tablas de datos.
 *
 * ## Componentes
 * - `Table`: Contenedor principal con scroll horizontal
 * - `TableHeader`: Encabezado de la tabla
 * - `TableBody`: Cuerpo con filas de datos
 * - `TableFooter`: Pie para totales
 * - `TableRow`: Fila individual
 * - `TableHead`: Celda de encabezado
 * - `TableCell`: Celda de datos
 * - `TableCaption`: Título descriptivo
 */
const meta: Meta<typeof Table> = {
  title: 'UI/Table',
  component: Table,
  tags: ['autodocs'],
  parameters: {
    docs: {
      description: {
        component: 'Sistema de componentes para tablas de datos con estilos consistentes.',
      },
    },
  },
};

export default meta;
type Story = StoryObj<typeof Table>;

const pacientes = [
  { id: 1, nombre: 'Max', especie: 'Canino', raza: 'Labrador', propietario: 'Juan Pérez' },
  { id: 2, nombre: 'Luna', especie: 'Felino', raza: 'Persa', propietario: 'María García' },
  { id: 3, nombre: 'Rocky', especie: 'Canino', raza: 'Bulldog', propietario: 'Carlos López' },
  { id: 4, nombre: 'Michi', especie: 'Felino', raza: 'Siamés', propietario: 'Ana Martínez' },
];

/**
 * Tabla básica con datos
 */
export const Default: Story = {
  render: () => (
    <Table>
      <TableCaption>Lista de pacientes de la clínica</TableCaption>
      <TableHeader>
        <TableRow>
          <TableHead className="w-[100px]">ID</TableHead>
          <TableHead>Nombre</TableHead>
          <TableHead>Especie</TableHead>
          <TableHead>Raza</TableHead>
          <TableHead className="text-right">Propietario</TableHead>
        </TableRow>
      </TableHeader>
      <TableBody>
        {pacientes.map((paciente) => (
          <TableRow key={paciente.id}>
            <TableCell className="font-medium">{paciente.id}</TableCell>
            <TableCell>{paciente.nombre}</TableCell>
            <TableCell>{paciente.especie}</TableCell>
            <TableCell>{paciente.raza}</TableCell>
            <TableCell className="text-right">{paciente.propietario}</TableCell>
          </TableRow>
        ))}
      </TableBody>
    </Table>
  ),
};

/**
 * Tabla con badges de estado
 */
export const WithBadges: Story = {
  render: () => (
    <Table>
      <TableHeader>
        <TableRow>
          <TableHead>Nombre</TableHead>
          <TableHead>Especie</TableHead>
          <TableHead>Estado</TableHead>
        </TableRow>
      </TableHeader>
      <TableBody>
        <TableRow>
          <TableCell>Max</TableCell>
          <TableCell>
            <Badge variant="outline" className="bg-blue-50 text-blue-700">
              Canino
            </Badge>
          </TableCell>
          <TableCell>
            <Badge variant="default">Activo</Badge>
          </TableCell>
        </TableRow>
        <TableRow>
          <TableCell>Luna</TableCell>
          <TableCell>
            <Badge variant="outline" className="bg-purple-50 text-purple-700">
              Felino
            </Badge>
          </TableCell>
          <TableCell>
            <Badge variant="secondary">En tratamiento</Badge>
          </TableCell>
        </TableRow>
      </TableBody>
    </Table>
  ),
};

/**
 * Tabla con footer para totales
 */
export const WithFooter: Story = {
  render: () => (
    <Table>
      <TableHeader>
        <TableRow>
          <TableHead>Servicio</TableHead>
          <TableHead className="text-right">Precio</TableHead>
        </TableRow>
      </TableHeader>
      <TableBody>
        <TableRow>
          <TableCell>Consulta general</TableCell>
          <TableCell className="text-right">$50,000</TableCell>
        </TableRow>
        <TableRow>
          <TableCell>Vacunación</TableCell>
          <TableCell className="text-right">$80,000</TableCell>
        </TableRow>
        <TableRow>
          <TableCell>Desparasitación</TableCell>
          <TableCell className="text-right">$35,000</TableCell>
        </TableRow>
      </TableBody>
      <TableFooter>
        <TableRow>
          <TableCell>Total</TableCell>
          <TableCell className="text-right font-bold">$165,000</TableCell>
        </TableRow>
      </TableFooter>
    </Table>
  ),
};

/**
 * Tabla vacía
 */
export const Empty: Story = {
  render: () => (
    <Table>
      <TableHeader>
        <TableRow>
          <TableHead>Nombre</TableHead>
          <TableHead>Especie</TableHead>
          <TableHead>Propietario</TableHead>
        </TableRow>
      </TableHeader>
      <TableBody>
        <TableRow>
          <TableCell colSpan={3} className="text-center text-muted-foreground h-24">
            No hay pacientes registrados
          </TableCell>
        </TableRow>
      </TableBody>
    </Table>
  ),
};
