export interface Vehiculo {
  id?: number;
  placa: string;
  marca: string;
  modelo: string;
  anioFabricacion: number;
  capacidadPasajeros: number;
  tipoServicio: string;
  estado?: string;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}
