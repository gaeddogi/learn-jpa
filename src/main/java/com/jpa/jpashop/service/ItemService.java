package com.jpa.jpashop.service;

import com.jpa.jpashop.controller.BookForm;
import com.jpa.jpashop.domain.item.Book;
import com.jpa.jpashop.domain.item.Item;
import com.jpa.jpashop.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;

    @Transactional
    public void saveItem(Item item) {
        itemRepository.save(item);
    }

    public Item findOnd(Long id) {
        return itemRepository.findOne(id);
    }

    public List<Item> findAll() {
        return itemRepository.findAll();
    }

    @Transactional
    public void updateItem(Long id, BookForm form) {
        Book findItem = (Book) itemRepository.findOne(id);
//        findItem.change(name, price, stockQuantity);
        findItem.setName(form.getName());
        findItem.setAuthor(form.getAuthor());
        findItem.setStockQuantity(form.getStockQuantity());
    }
}
