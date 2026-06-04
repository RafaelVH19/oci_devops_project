/**
 * Configuración global que Jest ejecuta antes de cada archivo de prueba.
 * Registra matchers de Testing Library y polyfills que jsdom no trae por defecto.
 */
import '@testing-library/jest-dom';
import { TextDecoder, TextEncoder } from 'util';

// React Router 7 y otros paquetes esperan TextEncoder en el entorno de prueba.
if (typeof globalThis.TextEncoder === 'undefined') {
  globalThis.TextEncoder = TextEncoder;
  globalThis.TextDecoder = TextDecoder;
}

// demoStore usa structuredClone; en Node/jsdom antiguo no existe nativamente.
if (typeof globalThis.structuredClone === 'undefined') {
  globalThis.structuredClone = (value) => JSON.parse(JSON.stringify(value));
}
