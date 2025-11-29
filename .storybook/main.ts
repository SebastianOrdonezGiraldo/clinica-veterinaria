import type { StorybookConfig } from '@storybook/react-vite';
import { mergeConfig } from 'vite';
import path from 'path';

/**
 * Configuración principal de Storybook
 *
 * Define las ubicaciones de stories, addons y opciones del framework.
 * Utiliza Vite como bundler para compatibilidad con el proyecto principal.
 */
const config: StorybookConfig = {
  stories: ['../apps/frontend/src/**/*.stories.@(js|jsx|ts|tsx|mdx)'],
  addons: [
    '@storybook/addon-links',
    '@storybook/addon-essentials',
    '@storybook/addon-interactions',
    '@storybook/addon-a11y',
  ],
  framework: {
    name: '@storybook/react-vite',
    options: {},
  },
  docs: {},
  typescript: {
    reactDocgen: 'react-docgen-typescript',
  },
  viteFinal: async (config) => {
    return mergeConfig(config, {
      resolve: {
        alias: {
          '@shared': path.resolve(__dirname, '../apps/frontend/src/shared'),
          '@core': path.resolve(__dirname, '../apps/frontend/src/core'),
          '@features': path.resolve(__dirname, '../apps/frontend/src/features'),
        },
      },
    });
  },
};

export default config;
