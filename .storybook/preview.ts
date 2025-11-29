import type { Preview } from '@storybook/react';
import '../apps/frontend/src/index.css';

/**
 * Configuración de preview para Storybook
 *
 * Define parámetros globales para todas las stories:
 * - Detecta automáticamente actions con prefijo 'on'
 * - Configura controles para colores y fechas
 * - Aplica estilos globales de Tailwind CSS
 */
const preview: Preview = {
  parameters: {
    actions: { argTypesRegex: '^on[A-Z].*' },
    controls: {
      matchers: {
        color: /(background|color)$/i,
        date: /Date$/,
      },
    },
    backgrounds: {
      default: 'light',
      values: [
        { name: 'light', value: '#ffffff' },
        { name: 'dark', value: '#1a1a1a' },
      ],
    },
    layout: 'centered',
  },
  tags: ['autodocs'],
};

export default preview;
