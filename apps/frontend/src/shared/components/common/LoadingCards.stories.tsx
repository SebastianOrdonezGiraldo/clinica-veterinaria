import type { Meta, StoryObj } from '@storybook/react';
import { LoadingCards } from './LoadingCards';

/**
 * LoadingCards muestra un grid de tarjetas skeleton durante la carga de datos.
 *
 * ## Uso
 * Útil para mejorar la experiencia de usuario mientras se cargan listas
 * de tarjetas como pacientes, propietarios o citas.
 */
const meta: Meta<typeof LoadingCards> = {
  title: 'Common/LoadingCards',
  component: LoadingCards,
  tags: ['autodocs'],
  argTypes: {
    count: {
      control: { type: 'number', min: 1, max: 12 },
      description: 'Número de tarjetas skeleton a mostrar',
      table: {
        defaultValue: { summary: '6' },
      },
    },
  },
  parameters: {
    docs: {
      description: {
        component: 'Componente de carga skeleton para grids de tarjetas.',
      },
    },
    layout: 'padded',
  },
};

export default meta;
type Story = StoryObj<typeof LoadingCards>;

/**
 * Estado de carga por defecto (6 tarjetas)
 */
export const Default: Story = {
  args: {},
};

/**
 * 3 tarjetas skeleton
 */
export const ThreeCards: Story = {
  args: {
    count: 3,
  },
};

/**
 * 9 tarjetas skeleton (grid completo)
 */
export const NineCards: Story = {
  args: {
    count: 9,
  },
};

/**
 * 1 tarjeta skeleton
 */
export const SingleCard: Story = {
  args: {
    count: 1,
  },
};
