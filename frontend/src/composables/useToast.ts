type ToastType = 'success' | 'error' | 'warning' | 'info'

export function useToast() {
  const toast = (message: string, type: ToastType = 'info') => {
    ;(window as any).showToast?.(message, type)
  }
  return { toast }
}
