export interface BlobRef {
  id: string;
  location: string; // Add this ourselves from Location header.
  name: string;
  mimeType: string;
  hash: number;
}