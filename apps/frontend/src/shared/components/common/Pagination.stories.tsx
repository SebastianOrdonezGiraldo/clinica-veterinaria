import type { Meta, StoryObj } from '@storybook/react';
import { useState } from 'react';
import { Pagination } from './Pagination';

/**
 * Pagination proporciona navegación entre páginas de datos.
 *
 * ## Características
 * - Muestra rango de elementos visibles
 * - Botones anterior/siguiente
 * - Números de página con navegación directa
 * - Se adapta a diferentes totales de páginas
 */
const meta: Meta<typeof Pagination> = {
  title: 'Common/Pagination',
  component: Pagination,
  tags: ['autodocs'],
  argTypes: {
    currentPage: {
      control: { type: 'number', min: 1 },
      description: 'Página actual (1-indexed)',
    },
    totalPages: {
      control: { type: 'number', min: 1 },
      description: 'Total de páginas',
    },
    itemsPerPage: {
      control: { type: 'number', min: 1 },
      description: 'Elementos por página',
    },
    totalItems: {
      control: { type: 'number', min: 0 },
      description: 'Total de elementos',
    },
  },
  parameters: {
    docs: {
      description: {
        component: 'Componente de paginación para listas y tablas.',
      },
    },
    layout: 'padded',
  },
};

export default meta;
type Story = StoryObj<typeof Pagination>;

/**
 * Paginación con 10 páginas
 */
export const Default: Story = {
  args: {
    currentPage: 1,
    totalPages: 10,
    itemsPerPage: 10,
    totalItems: 100,
    onPageChange: () => {},
  },
};

/**
 * Primera página
 */
export const FirstPage: Story = {
  args: {
    currentPage: 1,
    totalPages: 5,
    itemsPerPage: 10,
    totalItems: 50,
    onPageChange: () => {},
  },
};

/**
 * Página del medio
 */
export const MiddlePage: Story = {
  args: {
    currentPage: 5,
    totalPages: 10,
    itemsPerPage: 10,
    totalItems: 100,
    onPageChange: () => {},
  },
};

/**
 * Última página
 */
export const LastPage: Story = {
  args: {
    currentPage: 10,
    totalPages: 10,
    itemsPerPage: 10,
    totalItems: 100,
    onPageChange: () => {},
  },
};

/**
 * Pocas páginas (menos de 5)
 */
export const FewPages: Story = {
  args: {
    currentPage: 2,
    totalPages: 3,
    itemsPerPage: 10,
    totalItems: 25,
    onPageChange: () => {},
  },
};

/**
 * Muchas páginas
 */
export const ManyPages: Story = {
  args: {
    currentPage: 50,
    totalPages: 100,
    itemsPerPage: 20,
    totalItems: 2000,
    onPageChange: () => {},
  },
};

/**
 * Paginación interactiva
 */
export const Interactive: Story = {
  render: function InteractivePagination() {
    const [currentPage, setCurrentPage] = useState(1);
    const totalPages = 10;
    const itemsPerPage = 10;
    const totalItems = 100;

    return (
      <div className="space-y-4">
        <div className="text-center">
          <p className="text-muted-foreground">Página actual: {currentPage}</p>
        </div>
        <Pagination
          currentPage={currentPage}
          totalPages={totalPages}
          itemsPerPage={itemsPerPage}
          totalItems={totalItems}
          onPageChange={setCurrentPage}
        />
      </div>
    );
  },
};
