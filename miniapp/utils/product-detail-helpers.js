export function buildMenuOptions({ isOwner, isOffShelf }) {
  if (isOwner) {
    const toggleItem = isOffShelf
      ? { label: '上架', action: 'onShelf' }
      : { label: '下架', action: 'offShelf' }
    const items = ['编辑', toggleItem.label, '删除']
    const actions = ['edit', toggleItem.action, 'delete']
    return { items, actions }
  }
  return {
    items: ['分享', '举报'],
    actions: ['share', 'report']
  }
}

export function buildSharePayload(detail) {
  const id = detail && detail.id ? String(detail.id) : ''
  const title = (detail && detail.title) || '商品详情'
  const imageUrl =
    (detail && detail.images && detail.images[0]) ||
    (detail && detail.coverImage) ||
    ''
  return {
    title,
    path: `/pages/product/detail/detail?id=${id}`,
    imageUrl
  }
}

export default {
  buildMenuOptions,
  buildSharePayload
}
